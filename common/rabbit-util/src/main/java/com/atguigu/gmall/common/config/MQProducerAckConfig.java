package com.atguigu.gmall.common.config;


import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 该注解修饰一个非静态的void（）方法,在服务器加载Servlet的时候运行，
    // 并且只会被服务器执行一次在构造函数之后执行，init（）方法之前执行。
    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback(this::returnedMessage);
        rabbitTemplate.setConfirmCallback(this::confirm);
    }

    /**
     *消息成功发送到交换机上
     * @param correlationData  带有id标识的数据载体
     * @param ack  判断消息是否发送成功
     * @param cause  消息发送失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("消息发送成功：" + JSON.toJSONString(correlationData));
        } else {
            System.out.println("消息发送失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));

        }
    }

    /**
     *消息没有成功发送到队列中则会执行当前这个方法
     * @param message
     * @param replyCode  回复码
     * @param replyText  回复消息
     * @param exchange  交换机
     * @param routingKey  路由key
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
    }
}
