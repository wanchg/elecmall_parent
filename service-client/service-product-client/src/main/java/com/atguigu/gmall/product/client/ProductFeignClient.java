package com.atguigu.gmall.product.client;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//在item模块中能正常实现，但在这个模块就调用不到
//@Component
//@FeignClient(name = "service-product",fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    //获取sku基本信息
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(value = "skuId") Long skuId);

    //获取分类
    @GetMapping("api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    //查询商品价格为了最新的数据
    @GetMapping("api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId);

    //根据spuId，skuId查询销售属性和销售属性值
    @GetMapping("api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId);

    //根据spuId 获取销售属性值和skuId的组合
    @GetMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId);

    //根据id查询品牌信息
    @GetMapping("api/product/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademarkByTmId(@PathVariable Long tmId);
    //根据skuid查询平台属性，平台属性值
    @GetMapping("api/product/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);

}
