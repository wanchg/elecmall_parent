package com.atguigu.gmall.item;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.item.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@Component
//@FeignClient(name = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {
    //编写controller给web-all使用
    @GetMapping("api/item/{skuId}")
    public R getItemById(@PathVariable Long skuId);
}
