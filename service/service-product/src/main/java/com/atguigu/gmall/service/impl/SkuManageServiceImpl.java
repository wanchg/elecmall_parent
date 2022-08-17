package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.mapper.SkuImageMapper;
import com.atguigu.gmall.mapper.SkuInfoMapper;
import com.atguigu.gmall.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.service.SkuManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class SkuManageServiceImpl implements SkuManageService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //获取前端传的数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        //赋默认值
        skuInfo.setIsSale(0);
        skuInfoMapper.insert(skuInfo);
        Long id = skuInfo.getId();
        Long spuId = skuInfo.getSpuId();
        if (!StringUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue:skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(id);
                skuSaleAttrValue.setSpuId(spuId);
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        if (!StringUtils.isEmpty(skuImageList)){
            for (SkuImage skuImage:skuImageList) {
                skuImage.setSkuId(id);
                skuImageMapper.insert(skuImage);

            }
        }

        if (!StringUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue:skuAttrValueList){
                skuAttrValue.setSkuId(id);
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

    }

    @Override
    public IPage<SkuInfo> getSkuListByPage(Long current, Long limit) {
        Page<SkuInfo> page = new Page<>();
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(page, null);
        return skuInfoIPage;
    }

    //商品上架
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        //发送消息  餐参数一：交换机 二：路由key  三：发送消息主体
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_UPPER,skuId);
    }

    //商品下架
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //发送消息  餐参数一：交换机 二：路由key  三：发送消息主体
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);
    }


    @Override
    @GmallCache(prefix = "skuInfo:")
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = null;
        //检查缓存
        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

        if (StringUtils.isEmpty(skuInfo)){
            //查询数据，并放入缓存
            String skuInfoLock = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
            RLock lock = redissonClient.getLock(skuInfoLock);
            lock.lock(10, TimeUnit.SECONDS);


            try {
                //获取skuinfo
                skuInfo = skuInfoMapper.selectById(skuId);
                //获取sku图片
                QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
                skuImageQueryWrapper.eq("sku_id",skuId);
                List<SkuImage> skuImages = skuImageMapper.selectList(skuImageQueryWrapper);
                if (skuInfo != null){
                    skuInfo.setSkuImageList(skuImages);
                }

                if (skuInfo == null){
                    //解决缓存穿透
                    SkuInfo skuInfo1 = new SkuInfo();
                    //因为skuinfo已经序列化了，在redisTemplate的配置类中会转成json格式，也就是字符串
                    redisTemplate.opsForValue().set(skuKey,skuInfo1);
                    return skuInfo1;
                }

                //把查到的值放到缓存中
                redisTemplate.opsForValue().set(skuKey,skuInfo);
                return skuInfo;
            } finally {
                lock.unlock();
            }
        }
        return skuInfo;

    }

   /* @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = null;
        //检查缓存
        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

        try {
            if (StringUtils.isEmpty(skuInfo)){
                //查询数据，并放入缓存
                //设置锁
                String uuid = UUID.randomUUID().toString();
                String skuLockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                Boolean status = redisTemplate.opsForValue().setIfAbsent( skuLockKey,uuid, 10, TimeUnit.SECONDS);


                if (status){
                    //获取数据库中的skuinfo
                     skuInfo = skuInfoMapper.selectById(skuId);
                    //获取sku图片
                    QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
                    skuImageQueryWrapper.eq("sku_id",skuId);
                    List<SkuImage> skuImages = skuImageMapper.selectList(skuImageQueryWrapper);
                    if (skuInfo != null){
                        skuInfo.setSkuImageList(skuImages);
                    }


                    //没有查到，设置空值，防止缓存穿透
                    if (skuInfo == null){
                     SkuInfo skuInfo1 = new SkuInfo();
                      redisTemplate.opsForValue().set(skuKey,skuInfo1,10,TimeUnit.SECONDS);
                      return skuInfo1;
                    }
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);


                    //使用lua释放锁
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    //2.创建脚本对象
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
                    //3.设置lua脚本
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);  //返回值类型
                    //4.redis 调用LUA 脚本
                    redisTemplate.execute(redisScript, Arrays.asList("skuLockKey"),uuid);

                    return skuInfo;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果缓存中有数据，就取缓存中的
        return skuInfo;

    }*/

    @Override
    @GmallCache(prefix = "skuPrice:")
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        BigDecimal price = skuInfo.getPrice();
        return price;
    }

    @Override
    @GmallCache(prefix = "skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        //创建map存数据
        HashMap<Object, Object> map = new HashMap<>();
        List<Map> skuMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);

        //"126|124":"46"
        if (!StringUtils.isEmpty(skuMap)){
            for (Map maps:skuMap) {
                map.put(maps.get("value_ids"),maps.get("sku_id"));

            }
        }
        return map;
    }
}
