package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(value = "service-cart" ,fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    //添加购物车
    //不需要HttpServletRequest ，在feign传递时，拦截器中处理了
    @PostMapping("api/cart/addToCart/{skuId}/{skuNum}")
    public R addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum );

    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId);

    //通过userId 查询数据库并放入缓存
    @GetMapping("api/cart/loadCartCache/{userId}")
    public List<CartInfo> loadCartCache(@PathVariable String userId);
}
