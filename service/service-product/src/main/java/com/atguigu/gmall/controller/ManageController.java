package com.atguigu.gmall.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.product.vo.BaseAttrAndValueVo;
import com.atguigu.gmall.service.ManageService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//基础信息控制器
@RestController
@RequestMapping("/admin/product")

public class ManageController {
    @Autowired
    private ManageService manageService;

    //先加载所有的一级分类数据!
    @GetMapping("/getCategory1")
    public R getAllOneList(){
        List<BaseCategory1> goodsOne =  manageService.getAllOneList();
        return R.ok(goodsOne).message("成功");
    }
    //通过选择一级分类Id 数据加载二级分类数据!
    @GetMapping("/getCategory2/{oneId}")
    public R getAllTwoListByOneId(@PathVariable Long oneId){
        List<BaseCategory2> goodsTwo = manageService.getAllTwoListByOneId(oneId);
        return R.ok(goodsTwo).message("成功");
    }

    //通过选择二级分类数据加载三级分类数据!
    @GetMapping("/getCategory3/{twoId}")
    public R getAllThreeListByTwoId(@PathVariable Long twoId){
        List<BaseCategory3> goodsThree = manageService.getAllThreeListByTwoId(twoId);
        return R.ok(goodsThree).message("成功");
    }
    //根据分类Id加载平台属性列表和对应的属性值
    @GetMapping("/attrInfoList/{oneId}/{twoId}/{threeId}")
    public R getValueByAllId(@PathVariable Long oneId,@PathVariable Long twoId,@PathVariable Long threeId){
        List<BaseAttrAndValueVo> baseAttrAndValueVo = manageService.getValueByAllId(oneId,twoId,threeId);
        return R.ok(baseAttrAndValueVo).message("成功");
    }

    //添加平台属性或修改平台属性
    @PostMapping("/saveAttrInfo")
    public R saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        Long id = baseAttrInfo.getId();
        if (id == null){
            manageService.saveAttrInfo(baseAttrInfo);
        }else{
            manageService.updateAttrInfo(baseAttrInfo);
        }

        return R.ok().code(200).message("成功");
    }

    //根据平台id获取平台属性
    @GetMapping("/getAttrValueList/{attrId}")
    public R getAttrValueList(@PathVariable Long attrId){
       List<BaseAttrValue> valueList = manageService.getAttrValueList(attrId);
       return R.ok(valueList).message("成功").code(200);
    }

    //****************************商品属性**************
    @GetMapping("/baseSaleAttrList")
    public R baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.baseSaleAttrList();
        return R.ok(baseSaleAttrList).code(200).message("成功");
    }

    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("/getBaseCategoryList")
    public R getBaseCategoryList(){
        List<JSONObject> list = manageService.getBaseCategoryList();
        return R.ok(list);
    }


}
