package com.atguigu.gmall.cart.controller;


import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    private CartInfoService cartInfoService;

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public R addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum, HttpServletRequest request){
        /*在前端没有获取userId，在网关中已经获取到了
          String userId = request.getHeader("userId");
          也可以用自定义的类获取，在common工程中

           如果用户没有登录，要给一个临时id
        */
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //用户没有登录,从请求头中获取临时id
            //临时id是前端用一种类似uuid的做法获取到的
            userId = AuthContextHolder.getUserTempId(request);

        }
        cartInfoService.addToCart(userId,skuId,skuNum);
        return R.ok().message("成功").code(200);
    }

    //查询购物车
    @GetMapping("cartList")
    public R cartList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartList = cartInfoService.getCartList(userId, userTempId);
        return R.ok(cartList).code(200).message("成功");
    }

    /**
     * 更新选中状态
     *
     * @param skuId
     * @param isChecked
     * @param request
     * @return
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public R checkCart(@PathVariable Long skuId,
                       @PathVariable Integer isChecked, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartInfoService.checkCart(userId,isChecked,skuId);
        return R.ok().message("成功").code(200);
    }

    @DeleteMapping("deleteCart/{skuId}")
    public R deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //删除
        cartInfoService.deleteCartList(userId,skuId);
        return R.ok();
    }

    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId) {
        return cartInfoService.getCartCheckedList(userId);
    }

    //通过userId 查询数据库并放入缓存
    @GetMapping("loadCartCache/{userId}")
    public List<CartInfo> loadCartCache(@PathVariable String userId){
        List<CartInfo> cartInfos = cartInfoService.loadCartCache(userId);
        return cartInfos;
    }
}

