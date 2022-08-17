package com.atguigu.gmall.weball.controller;


import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.weball.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //http://payment.gmall.com/pay.html?orderId=192
    @GetMapping("pay.html")
    public String payIndex(HttpServletRequest request){ //可以使用request获取
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        //  需要${orderInfo}
        request.setAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    @GetMapping("pay/success.html")
    public String success(){

        //返回页面
        return "payment/success";
    }
}
