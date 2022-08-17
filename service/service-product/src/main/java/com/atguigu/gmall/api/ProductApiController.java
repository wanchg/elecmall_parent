package com.atguigu.gmall.api;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.SkuManageService;
import com.atguigu.gmall.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Autowired
    private SkuManageService skuManageService;
    @Autowired
    private ManageService manageService;
    @Autowired
    private TrademarkService trademarkService;


    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        SkuInfo skuInfo = skuManageService.getSkuInfo(skuId);
        return skuInfo;
    }

    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        BaseCategoryView baseCategoryView = manageService.getCategoryView(category3Id);
        return baseCategoryView;
    }

    //查询商品价格为了最新的数据
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        BigDecimal price = skuManageService.getSkuPrice(skuId);
        return price;
    }

    //根据spuId，skuId查询销售属性和销售属性值
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList;
    }

    //根据spuId 获取销售属性值和skuId的组合
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        Map skuMap = skuManageService.getSkuValueIdsMap(spuId);
        return skuMap;
    }

    //根据id查询品牌信息
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademarkByTmId(@PathVariable Long tmId){
        BaseTrademark baseTrademark = trademarkService.getTrademarkByTmId(tmId);
        return baseTrademark;
    }
    //根据skuid查询平台属性，平台属性值
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        List<BaseAttrInfo> attrList = manageService.getAttrList(skuId);
        return attrList;
    }
}
