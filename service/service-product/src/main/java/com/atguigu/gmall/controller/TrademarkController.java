package com.atguigu.gmall.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.service.TrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//品牌控制器
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class TrademarkController {

    @Autowired
    private TrademarkService trademarkService;

    //http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
    //分页查询品牌带分页
    @GetMapping("/{current}/{limit}")
    public R baseTrademark(@PathVariable Long current,@PathVariable Long limit){
        Page<BaseTrademark> page = new Page<>(current,limit);
        IPage<BaseTrademark> trademarkIPage = trademarkService.baseTrademark(page);
        return R.ok(trademarkIPage).message("成功").code(200);
    }

    //添加品牌
    @PostMapping("/save")
    public R saveTrademark(@RequestBody BaseTrademark baseTrademark){
        trademarkService.saveTrademark(baseTrademark);
        return R.ok().code(200).message("成功");
    }

    //修改品牌
    @PutMapping("update")
    public R updateTrademark(@RequestBody BaseTrademark baseTrademark){
      trademarkService.updateTrademark(baseTrademark);
      return R.ok().message("成功").code(200);
    }

    //删除品牌
    @DeleteMapping("/remove/{id}")
    public R removeTrademark(@PathVariable Long id){
        trademarkService.removeById(id);
        return R.ok().code(200).message("成功");
    }

    //根据id获取品牌数据
    @GetMapping("/get/{id}")
    public R getTrademark(@PathVariable Long id){
        BaseTrademark baseTrademark = trademarkService.getById(id);
        return R.ok(baseTrademark).message("成功").code(200);
    }

    @GetMapping("/getTrademarkList")
    public R getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = trademarkService.getTrademarkList();
        return R.ok(baseTrademarkList).code(200).message("成功");
    }


}
