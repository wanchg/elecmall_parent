package com.atguigu.gmall.weball.client;


import com.atguigu.gmall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "service-item")
public interface ItemFeignClient {
    //编写controller给web-all使用
    @GetMapping("api/item/{skuId}")
    public R getItemById(@PathVariable Long skuId);
}
