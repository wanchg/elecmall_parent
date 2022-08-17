package com.atguigu.gmall.weball.client;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
@FeignClient(value = "service-order")
public interface OrderFeignClient {
    //确定订单
    //加上这个的auth需要登录
    @GetMapping("api/order/auth/trade")
    public R<Map<String,Object>> trade(HttpServletRequest request);

    //查询订单信息
    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);
}
