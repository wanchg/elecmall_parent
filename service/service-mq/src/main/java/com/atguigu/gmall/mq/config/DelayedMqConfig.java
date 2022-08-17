package com.atguigu.gmall.mq.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class DelayedMqConfig {
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    //队列
    @Bean
    public Queue deadQueue(){
        //使用插件，不需要在队列中设置参数
        return new Queue(queue_delay_1,true,false,false,null);
    }
    //交换机
    @Bean
    public CustomExchange exchange(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        //type不能随便写
        return new CustomExchange(exchange_delay,"x-delayed-message",true,false,map);
    }

    //绑定交换机和队列
    @Bean
    public Binding exchangeBinding(){
        return BindingBuilder.bind(deadQueue()).to(exchange()).with(routing_delay).noargs();
    }
}
