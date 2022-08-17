package com.atguigu.gmall.cart.service.impl;


import com.atguigu.gmall.cart.client.ProductFeignClient;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CartAsyncService cartAsyncService;

    //在购物车中添加商品
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addToCart(String userId, Long skuId, Integer skuNum) {
        /*
         * 如果购物车中有该商品就更新
         * 没有就添加
         * */
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        CartInfo cartInfo = null;

        // 判断缓存中是否有cartKey，先加载数据库中的数据放入缓存！
        if (!redisTemplate.hasKey(cartKey)) {
            this.loadCartCache(userId);
        }

        try {

            /*//查询数据库
            QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
            cartInfoQueryWrapper.eq("user_id",userId);
            cartInfoQueryWrapper.eq("sku_id",skuId);
            cartInfo = cartInfoMapper.selectOne(cartInfoQueryWrapper);*/
            //代码到这里，说明redis中已经有数据了
            //使用 hget key field
            //cartInfo = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());  和下面的一样
            cartInfo = (CartInfo)redisTemplate.opsForHash().get(cartKey, skuId.toString());
            if (cartInfo != null){
                //购物车中有,更新
                cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                cartInfo.setCartPrice(productFeignClient.getSkuPrice(skuId));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                //cartInfoMapper.updateById(cartInfo);


                cartAsyncService.updateAsyncCartInfo(cartInfo);
            }else {
                //没有，插入
                //获取sku信息
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

                CartInfo addCartInfo = new CartInfo();

                addCartInfo.setCartPrice(skuInfo.getPrice());
                addCartInfo.setSkuPrice(skuInfo.getPrice());
                addCartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                addCartInfo.setSkuId(skuId);
                addCartInfo.setIsChecked(1);   //默认选中
                addCartInfo.setSkuName(skuInfo.getSkuName());
                addCartInfo.setUserId(userId);
                addCartInfo.setSkuNum(skuNum);
                addCartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                addCartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

                //cartInfoMapper.insert(addCartInfo);
                //实现异步插入
                cartAsyncService.saveCartInfo(addCartInfo);
                //添加代码出现问题时，redis还是会添加进去,需要一个返回值判断是否添加成功
                //当flag为-1时，添加失败，为1时添加成功
                cartInfo = addCartInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果上面的代码出现了异常，没有这个if会有一个空值添加到redis
        if (cartInfo != null){
            //把数据添加到缓存
            //参数一：集合  参数二： 键  参数三：值
            // 给<key>集合中的  <field>键赋值<value>
            redisTemplate.opsForHash().put(cartKey, userId, cartInfo);

            //给这个集合一个过期时间
            redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        }

    }

    /**
     * 查询购物车
     * @param userId
     * @param userTempId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        /*
         * 1.查询redis
         * 如果查到了就返回
         * 2.没有查到就查询数据库并把数据放入redis
         * */
        List<CartInfo> cartInfos = new ArrayList<>();

        //判断用户id是否为空
        if (!StringUtils.isEmpty(userId)){
            //如果不为空，判断临时id是否为空，如果也不为空，就查询是否有购物车数据，并合并购物车
            if (StringUtils.isEmpty(userTempId)){

                cartInfos = getCartList(userId);
                return new ArrayList<>();
            }
            //获取临时购物车
            List<CartInfo> cartTempList = getCartList(userTempId);
            if (!CollectionUtils.isEmpty(cartTempList)){
                //合并购物车
                cartInfos = this.mergeToCartList(cartTempList, userId);
                //删除临时购物车
                this.deleteCartList(userTempId);
            }else{
                cartInfos = getCartList(userId);
            }

        }else {
            //用户id为空
            //判断用户临时id是否为空
            if (!StringUtils.isEmpty(userTempId)){
                cartInfos = getCartList(userTempId);
            }
        }

        return cartInfos;
    }

    @Override
    public void deleteCartList(String userId, Long skuId) {
        //删除数据库中的值
        cartAsyncService.deleteAsyncCartList(userId, skuId);
        //删除redis
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        if (redisTemplate.hasKey(cartKey)){
            redisTemplate.opsForHash().delete(cartKey,skuId.toString());
        }

    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //购物车数据从redis中获取，选中后去付款，一般redis不会过期
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;

        List<CartInfo> values = redisTemplate.opsForHash().values(cartKey);
        List<CartInfo> cartInfoList = values.stream().filter((cartInfo) -> {
            return cartInfo.getIsChecked() == 1;
        }).collect(Collectors.toList());
        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //异步修改数据库
        cartAsyncService.checkCart(userId,isChecked,skuId);
        //修改redis的数据
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo.getIsChecked() == null){
            cartInfo.setIsChecked(isChecked);
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);

            //重新设置过期时间
            redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE,TimeUnit.SECONDS);
        }
    }

    private void deleteCartList(String userTempId) {
        //删除数据库中的数据
        cartAsyncService.deleteAsyncCartList(userTempId);
        //删除redis中的数据
        if (redisTemplate.hasKey(userTempId)){
            redisTemplate.delete(userTempId);
        }


    }

    //合并购物车
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        /**
         * 1.根据userId获取到登录用户的购物车集合数据
         * 2.做合并处理，skuId 相同数量相加。不同添加;
         */

        List<CartInfo> cartInfos = new ArrayList<>();

        //登录时的购物车
        List<CartInfo> cartInfoLoginList = getCartList(userId);
        //把数据变成map做包含  key是SkuId  value是cartInfo
        Map<Long, CartInfo> longCartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, (cartInfo) -> {
            return cartInfo;
        }));

        //未登录时的数据
        for (CartInfo cartInfo:cartInfoNoLoginList) {
            Long skuId = cartInfo.getSkuId();
            if (longCartInfoMap.containsKey(skuId.toString())){
                //包含就更新数量
                CartInfo longCartInfo = longCartInfoMap.get(skuId);
                longCartInfo.setSkuNum(longCartInfo.getSkuNum()+cartInfo.getSkuNum());
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

                //选中状态以勾选为准
                if (cartInfo.getIsChecked() == 1){
                    longCartInfo.setIsChecked(1);
                }
                /*
                更新数据库,可能有id可能没有id  （因为可能是从redis中查询的）
                建议使用同步，loadCartCache会查询数据库。异步操作会在主线程执行后再去执行，查询的数据不是最新的
                如果想用异步，可以查询redis，但redis不能有过期时间
                */
                //cartAsyncService.updateAsyncCartInfo(longCartInfo);
                QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
                cartInfoQueryWrapper.eq("user_id",cartInfo.getUserId());
                cartInfoQueryWrapper.eq("sku_id",cartInfo.getSkuId());
                cartInfoMapper.update(cartInfo,cartInfoQueryWrapper);

                //通过userId 查询数据库并放入缓存
                cartInfos = this.loadCartCache(userId);
            }else {
                //不包含就插入,临时userid需要修改
                cartInfo.setUserId(userId);
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                //建议使用同步，loadCartCache会查询数据库。异步操作会在主线程执行后再去执行，查询的数据不是最新的
                cartInfoMapper.insert(cartInfo);
                //通过userId 查询数据库并放入缓存
                cartInfos = this.loadCartCache(userId);
            }
        }
        return cartInfos;
    }

    //真正的查询
    private List<CartInfo> getCartList(String userId){
        //redis的key
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;

        List<CartInfo> list = redisTemplate.opsForHash().values(cartKey);
        if (CollectionUtils.isEmpty(list)){
            //缓存中没有数据
            list = this.loadCartCache(userId);

        }
            //降序排序
            list.sort((cart1,cart2)-> {
                return DateUtil.truncatedCompareTo(cart1.getUpdateTime(),cart2.getUpdateTime(), Calendar.SECOND);
            });
            return list;

    }

    //通过userId 查询数据库并放入缓存
    public List<CartInfo> loadCartCache(String userId) {
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        //缓存中没有数据了，可能已经过了很长的时间，有可能实时价格变化了.要查询实时价格
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userId);
        cartInfoQueryWrapper.orderByDesc("update_time");
        List<CartInfo> cartInfos = cartInfoMapper.selectList(cartInfoQueryWrapper);

        if (CollectionUtils.isEmpty(cartInfos)){
            return new ArrayList<>();
        }
        //把数据放入redis并放入实时价格
        HashMap<String, CartInfo> hashMap = new HashMap<>();
        for (CartInfo cartInfo:cartInfos) {
            //放入实时价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            //redisTemplate.opsForHash().put(cartKey,cartInfo.getSkuId().toString(),cartInfo);
            hashMap.put(cartInfo.getSkuId().toString(),cartInfo);
        }

        //一次性放入多条数据
        redisTemplate.opsForHash().putAll(cartKey,hashMap);
        //设置过期时间
        redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);

        return cartInfos;

    }


}
