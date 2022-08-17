package com.atguigu.gmall.service;


import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.Map;

public interface SkuManageService {
    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> getSkuListByPage(Long current, Long limit);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuInfo(Long skuId);

    BigDecimal getSkuPrice(Long skuId);

    Map getSkuValueIdsMap(Long spuId);
}
