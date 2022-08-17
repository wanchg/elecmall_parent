package com.atguigu.gmall.weball.client;


import com.atguigu.gmall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "service-cart")
public interface CartFeignClient {

    //添加购物车
    //不需要HttpServletRequest ，在feign传递时，拦截器中处理了
    @PostMapping("addToCart/{skuId}/{skuNum}")
    public R addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum);
}
