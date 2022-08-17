package com.atguigu.gmall.list.receiver;


import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ListReceiver {
    @Autowired
    private SearchService searchService;

    //监听是否有消息，有消息会执行这个方法
    //商品上架
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void GoodsUpper(Long skuId, Message message, Channel channel) throws IOException {
        if (skuId != null){
            searchService.upperGoods(skuId);
        }
        //消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //商品下架
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void GoodsLower(Long skuId, Message message, Channel channel) throws IOException {
        if (skuId != null){
            searchService.lowerGoods(skuId);
        }
        //消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
