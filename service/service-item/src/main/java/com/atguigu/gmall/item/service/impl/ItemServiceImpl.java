package com.atguigu.gmall.item.service.impl;


import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.item.client.ListFeignClient;
import com.atguigu.gmall.item.client.ProductFeignClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ListFeignClient listFeignClient;

    //远程调用service-product-client
    @Override
    public Map<String, Object> getBySkuId(Long skuId) {

        //创建map
        HashMap<String, Object> result = new HashMap<>();


        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //获取sku基本信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            // 保存skuInfo
            result.put("skuInfo",skuInfo);
            return skuInfo;
        },threadPoolExecutor);

        CompletableFuture<Void> categoryView1 = skuInfoCompletableFuture.thenAccept((skuInfo) -> {
            //获取sku分类
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            //保存商品分类数据
            result.put("categoryView", categoryView);

        });

        CompletableFuture<Void> valuesSkuJson1 = skuInfoCompletableFuture.thenAccept((skuInfo -> {
            //获取skuId和skuvalue的组合
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            // 保存 json字符串
            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
            // 保存valuesSkuJson
            result.put("valuesSkuJson", valuesSkuJson);
        }));

        CompletableFuture<Void> spuSaleAttrList1 = skuInfoCompletableFuture.thenAccept((skuInfo) -> {
            //获取sku销售属性
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            // 保存数据
            result.put("spuSaleAttrList", spuSaleAttrList);
        });

        CompletableFuture<Void> price = CompletableFuture.runAsync(() -> {
            //获取sku价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            // 获取价格
            result.put("price", skuPrice);
        },threadPoolExecutor);

        CompletableFuture<Void> hotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryView1,
                valuesSkuJson1,
                spuSaleAttrList1,
                price,
                hotScoreCompletableFuture
        ).join();


        return result;
    }
}
