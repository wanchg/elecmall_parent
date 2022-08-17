package com.atguigu.gmall.weball.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.weball.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private ProductFeignClient productFeignClient;

    //http://www.gmall.com
    //http://www.gmall.com/index.html
    @GetMapping({"/","index.html"})
    public String index(HttpServletRequest request){
        R result = productFeignClient.getBaseCategoryList();
        List<JSONObject> data = (List<JSONObject>) result.getData();
        request.setAttribute("list",data);
        return "index/index";
    }
}
