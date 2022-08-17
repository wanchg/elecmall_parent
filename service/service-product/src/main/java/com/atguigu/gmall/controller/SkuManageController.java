package com.atguigu.gmall.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.service.SkuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private SkuManageService skuManageService;

    @PostMapping("/saveSkuInfo")
    public R saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuManageService.saveSkuInfo(skuInfo);
        return R.ok().message("成功").code(200);
    }

    //list/{page}/{limit}
    @GetMapping("list/{current}/{limit}")
    public R getSkuListByPage(@PathVariable Long current,@PathVariable Long limit){
        IPage<SkuInfo> skuInfoIPage = skuManageService.getSkuListByPage(current,limit);
        return R.ok(skuInfoIPage).code(200).message("成功");
    }

    //http://api.gmall.com/admin/product/onSale/{skuId}
    @GetMapping("/onSale/{skuId}")
    public R onSale(@PathVariable Long skuId){
        skuManageService.onSale(skuId);
        return R.ok().message("成功").code(200);
    }
    //http://api.gmall.com/admin/product/cancelSale/{skuId}
    @GetMapping("/cancelSale/{skuId}")
    public R cancelSale(@PathVariable Long skuId){
        skuManageService.cancelSale(skuId);
        return R.ok().message("成功").code(200);
    }
}
