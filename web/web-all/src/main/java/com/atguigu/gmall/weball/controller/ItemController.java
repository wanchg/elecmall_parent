package com.atguigu.gmall.weball.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.weball.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller  //需要返回视图
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){
        R<Map<String,Object>> item = itemFeignClient.getItemById(skuId);
        model.addAllAttributes(item.getData());
        return "item/index";
    }
}
