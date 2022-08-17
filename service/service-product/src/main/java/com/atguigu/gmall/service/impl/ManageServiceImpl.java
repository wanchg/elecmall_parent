package com.atguigu.gmall.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.mapper.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.product.vo.BaseAttrAndValueVo;
import com.atguigu.gmall.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;


    //获取一级分类
    @Override
    public List<BaseCategory1> getAllOneList() {
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(null);

        return baseCategory1List;
    }

    //获取二级分类
    @Override
    public List<BaseCategory2> getAllTwoListByOneId(Long oneId) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",oneId);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
        return baseCategory2List;
    }

    //获取三级分类
    @Override
    public List<BaseCategory3> getAllThreeListByTwoId(Long twoId) {
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id",twoId);
        List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
        return baseCategory3List;
    }

    @Override
    public List<BaseAttrAndValueVo> getValueByAllId(Long oneId, Long twoId, Long threeId) {
        List<BaseAttrAndValueVo> baseAttrAndValueVo = baseAttrInfoMapper.getValueByAllId(oneId,twoId,threeId);
        return baseAttrAndValueVo;
    }

    //一个方法中有两个插入语句要注意事务，需要加上事务注解
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        baseAttrInfoMapper.insert(baseAttrInfo);

        Long id = baseAttrInfo.getId();
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!StringUtils.isEmpty(attrValueList)){
            for (BaseAttrValue baseValue: attrValueList) {
                baseValue.setAttrId(id);
                baseAttrValueMapper.insert(baseValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",attrId);
        List<BaseAttrValue> valueList = baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);
        return valueList;
    }

    @Override
    public void updateAttrInfo(BaseAttrInfo baseAttrInfo) {
        Long id = baseAttrInfo.getId();
        //删除数据
        QueryWrapper<BaseAttrValue> attrValueQueryWrapper = new QueryWrapper<>();
        attrValueQueryWrapper.eq("attr_id",id);
        baseAttrValueMapper.delete(attrValueQueryWrapper);

        //修改属性名称
        QueryWrapper<BaseAttrInfo> baseAttrInfoQueryWrapper = new QueryWrapper<>();
        baseAttrInfoQueryWrapper.eq("id",id);
        baseAttrInfoMapper.update(baseAttrInfo,baseAttrInfoQueryWrapper);

        //修改属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue:attrValueList) {
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public HashMap<String, Object> getSpuListByLimitPage(Long page, Long limit, Long category3Id) {
        Page<SpuInfo> page1 = new Page<>(page,limit);

        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",category3Id);
        spuInfoQueryWrapper.orderByDesc("id");
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(page1, spuInfoQueryWrapper);


        HashMap<String, Object> map = new HashMap<>();

        long total = spuInfoIPage.getTotal();
        long size = spuInfoIPage.getSize();
        long current = spuInfoIPage.getCurrent();
        long pages = spuInfoIPage.getPages();
        List<SpuInfo> records = spuInfoIPage.getRecords();
        map.put("records",records);
        map.put("total",total);
        map.put("size",size);
        map.put("current",current);
        map.put("pages",pages);
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();


        spuInfoMapper.insert(spuInfo);
        Long id = spuInfo.getId();
        for (SpuImage spuImage:spuImageList) {
            spuImage.setSpuId(id);
            spuImageMapper.insert(spuImage);
        }
        for (SpuSaleAttr spuSaleAttr:spuSaleAttrList) {
            spuSaleAttr.setSpuId(id);
            spuSaleAttrMapper.insert(spuSaleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue:spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(id);
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);

            }

        }
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id",spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(spuImageQueryWrapper);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.spuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    //select * from base_category_view where category3Id=1;
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        return baseCategoryView;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        //创建json集合对象
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();

        //获取分类数据,查询视图
        //select * from base_category_view;
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        //初始化index
        int index = 1;
        //一级分类的值有很多，分组能去重.  key是一级分类id  value是分类的集合
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //获取一级分类id和值
        //entrySet()获取map中的一个映射项，映射项包含Key和Value
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator = category1Map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, List<BaseCategoryView>> entry = iterator.next();
            Long category1Id = entry.getKey();
            List<BaseCategoryView> category2List = entry.getValue();
            //获取一级分类名称
            String category1Name = category2List.get(0).getCategory1Name();

            //封装进jsonobject
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",category1Id);
            category1.put("categoryName",category1Name);
            //category1.put("categoryChild",);

            ArrayList<JSONObject> category2Child = new ArrayList<>();
            //获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = category2Map.entrySet().iterator();
            while (iterator1.hasNext()){
                Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
                Long category2Id = entry1.getKey();
                List<BaseCategoryView> category3List = entry1.getValue();
                //获取二级分类名称
                String category2Name = category3List.get(0).getCategory2Name();

                //封装进json
                JSONObject category2 = new JSONObject();
                category2.put("categoryId",category2Id);
                category2.put("categoryName",category2Name);
                
                //声明一个集合放入category2
                category2Child.add(category2);

                ArrayList<JSONObject> category3Child = new ArrayList<>();
                //获取三级分类数据
                category3List.forEach((baseCategoryView)->{
                    Long category3Id = baseCategoryView.getCategory3Id();
                    String category3Name = baseCategoryView.getCategory3Name();

                    //封装
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3Id);
                    category3.put("categoryName",category3Name);
                    category3Child.add(category3);
                });

                //把三级分类数据放入二级分类中
                category2.put("categoryChild",category3Child);
            }

            //把二级分类数据放入一级分类中
            category1.put("categoryChild",category2Child);

            jsonObjects.add(category1);
            //index迭代
            index++;

        }

        return jsonObjects;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
        return baseAttrInfoList;
    }


}
