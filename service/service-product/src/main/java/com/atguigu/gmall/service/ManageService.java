package com.atguigu.gmall.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.product.vo.BaseAttrAndValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;

public interface ManageService {
    List<BaseCategory1> getAllOneList();

    List<BaseCategory2> getAllTwoListByOneId(Long oneId);

    List<BaseCategory3> getAllThreeListByTwoId(Long twoId);

    List<BaseAttrAndValueVo> getValueByAllId(Long oneId, Long twoId, Long threeId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long attrId);

    void updateAttrInfo(BaseAttrInfo baseAttrInfo);

    HashMap<String, Object> getSpuListByLimitPage(Long page, Long limit, Long category3Id);

    void saveSpuInfo(SpuInfo spuInfo);

    List<BaseSaleAttr> baseSaleAttrList();

    List<SpuImage> spuImageList(Long spuId);

    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    BaseCategoryView getCategoryView(Long category3Id);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 获取全部分类信息
     * @return
     */
    List<JSONObject> getBaseCategoryList();
    /**
     * 通过skuId 集合来查询数据
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);
}
