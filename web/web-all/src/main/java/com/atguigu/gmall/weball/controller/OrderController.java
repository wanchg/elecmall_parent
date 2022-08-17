package com.atguigu.gmall.weball.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.weball.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class OrderController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    //确认订单
    @GetMapping("trade.html")
    public String trade(HttpServletRequest request, Model model){
        R<Map<String, Object>> trade = orderFeignClient.trade(request);
        Map<String, Object> data = trade.getData();

        model.addAllAttributes(data);

        return "order/trade";
    }
}
