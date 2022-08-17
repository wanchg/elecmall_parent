package com.atguigu.gmall.order.config;


import com.atguigu.gmall.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class OrderCancelMqConfig {

    //队列
    @Bean
    public Queue deadQueue(){
        //使用插件，不需要在队列中设置参数
        return new Queue(MqConst.QUEUE_ORDER_CANCEL,true,false,false,null);
    }
    //交换机
    @Bean
    public CustomExchange exchange(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        //type不能随便写
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,"x-delayed-message",true,false,map);
    }

    //绑定交换机和队列
    @Bean
    public Binding exchangeBinding(){
        return BindingBuilder.bind(deadQueue()).to(exchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
