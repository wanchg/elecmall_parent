package com.atguigu.gmall.item.impl;

import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.item.ItemFeignClient;
import org.springframework.stereotype.Component;


@Component
public class ItemDegradeFeignClient implements ItemFeignClient {

    @Override
    public R getItemById(Long skuId) {
        return null;
    }
}
