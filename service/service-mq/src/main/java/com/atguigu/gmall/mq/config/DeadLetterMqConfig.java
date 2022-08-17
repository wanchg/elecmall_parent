package com.atguigu.gmall.mq.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.HashMap;

@Configuration
public class DeadLetterMqConfig {

    // 声明一些变量
    /*一个正常交换机对应一个正常路由和一个正常队列
    * 一个死信交换机对应一个死信路由和一个死信队列
    * 死信交换机和正常交换机公用一个
    * */
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    //创建一个正常队列
    @Bean
    public Queue queue1(){
        //使用map封账额外的参数
        HashMap<String, Object> map = new HashMap<>();
        //队列的过期时间  单位：毫秒
        map.put("x-message-ttl",10*1000);
        //死信队列
        map.put("x-dead-letter-exchange",exchange_dead);
        //死信路由key
        map.put("x-dead-letter-routing-key",routing_dead_1);
        return new Queue(queue_dead_1,true,false,false,map);
    }

    //声明一个正常交换机
    @Bean
    public Exchange exchange1(){
        return new DirectExchange(exchange_dead,true,false);
    }

    //绑定正常交换机和队列
    @Bean
    public Binding binding1(){
        return BindingBuilder.bind(queue1()).to(exchange1()).with(routing_dead_2).noargs();
    }

    //声明一个死信队列
    @Bean
    public Queue queue2(){
        return new Queue(queue_dead_2,true,false,false,null);
    }

    //绑定死信交换机和队列
    @Bean
    public Binding binding2(){
        return BindingBuilder.bind(queue2()).to(exchange1()).with(routing_dead_1).noargs();
    }

}
