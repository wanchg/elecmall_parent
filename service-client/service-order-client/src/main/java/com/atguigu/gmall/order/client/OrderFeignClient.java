package com.atguigu.gmall.order.client;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {
    //确定订单
    //加上这个的auth需要登录
    @GetMapping("api/order/auth/trade")
    public R<Map<String,Object>> trade(HttpServletRequest request);

    //查询订单信息
    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);

    //更新订单状态
    @GetMapping("api/order/inner/updateOrderStatus/{orderId}/{processStatus}")
    public void updateOrderStatus(@PathVariable Long orderId,@PathVariable ProcessStatus processStatus);
}
