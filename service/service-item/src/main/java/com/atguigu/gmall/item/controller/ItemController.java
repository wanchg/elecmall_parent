package com.atguigu.gmall.item.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    //编写controller给web-all使用
    @GetMapping("{skuId}")
    public R getItemById(@PathVariable Long skuId){
        Map<String, Object> map = itemService.getBySkuId(skuId);
        return R.ok(map);
    }
}
