package com.atguigu.gmall.order.client.impl;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public R<Map<String, Object>> trade(HttpServletRequest request) {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {

    }
}
