package com.atguigu.gmall.mq.receiver;


import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ConfirmReceiver {

    //监听器,需要绑定消息
    /*
    value是队列 参数一：队列名称，二：持久化，三：自动删除
    exchange是交换机，参数一：交换机的名称
    key是路由key
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    //参数一接受到的消息，参数二：消息类  参数三：信道
   public void getMeg(String mes, Message message, Channel channel) throws IOException {

        try {
            System.out.println("接收到的消息"+new String(message.getBody()));
            ;
            System.out.println("接收到的消息"+mes);
            //手动确认ack

          Long  deliveryTag = message.getMessageProperties().getDeliveryTag();
            //参数一：手动确认，参数二：false代表一个一个确认，true表示批量确认
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {

            //参数二：false代表一个一个确认，true表示批量确认。参数三：是否重回队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }

    //监听死信队列queue_dead_2
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMeg1(String meg,Message message,Channel channel) throws IOException {
        try {
            System.out.println(meg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {

            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }

    //监听死信队列queue.delay.1  使用插件
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getMeg2(String meg,Message message,Channel channel) throws IOException {
        try {
            System.out.println(meg);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.format(new Date());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {

            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }
}
