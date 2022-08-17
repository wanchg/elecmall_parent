package com.atguigu.gmall.order.receiver;


import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


//消费消息
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId){
        if (orderId != null){
            //查询订单信息
            OrderInfo orderInfo = orderService.getById(orderId);
            //判断订单状态，如果是未付款就修改状态
            if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                QueryWrapper<OrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
                orderInfoQueryWrapper.eq("id",orderId);
                orderInfo.setProcessStatus(OrderStatus.CLOSED.name());
                orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
                orderService.update(orderInfo,orderInfoQueryWrapper);

            }
        }
    }

}
