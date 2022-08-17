package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;


@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public R incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public R list(SearchParam listParam) {
        return null;
    }

    @Override
    public R upperGoods(Long skuId) {
        return null;
    }

    @Override
    public R lowerGoods(Long skuId) {
        return null;
    }
}
