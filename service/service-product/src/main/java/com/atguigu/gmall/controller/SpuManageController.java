package com.atguigu.gmall.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


//spu控制器
@RestController
@RequestMapping("/admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    @GetMapping("/{page}/{limit}")
    public R getSpuListByLimitPage(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   @RequestParam("category3Id") Long category3Id){

        HashMap<String, Object> spuInfoList = manageService.getSpuListByLimitPage(page,limit,category3Id);
        return R.ok(spuInfoList).code(200).message("成功");

    }

    @PostMapping("/saveSpuInfo")
    public R saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return R.ok().message("成功").code(200);
    }

    //根据spuid获取图片
    @GetMapping("/spuImageList/{spuId}")
    public R spuImageList(@PathVariable Long spuId){
       List<SpuImage> spuImageList = manageService.spuImageList(spuId);
       return R.ok(spuImageList).message("成功").code(200);
    }

    //根据spuid获取销售属性和属性值
    @GetMapping("/spuSaleAttrList/{spuId}")
    public R spuSaleAttrList(@PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.spuSaleAttrList(spuId);
        return R.ok(spuSaleAttrList).code(200).message("成功");
    }




}
