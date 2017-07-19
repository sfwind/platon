package com.iquanwai.platon.biz.util.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import lombok.Getter;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Created by justin on 17/1/19.
 */
public class RabbitMQReceiver {
    private Connection connection;
    @Getter
    private Channel channel;
    private String queue;
    private int port = 5672;

    public void init(String queue, String topic, String ipAddress, Integer port){
        Assert.notNull(topic, "消息主题不能为空");
        Assert.notNull(ipAddress, "rabbit ip不能为空");
        destroy();
        if (port != null) {
            this.port = port;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAddress);
        factory.setPort(this.port);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            //交换机声明,广播形式
            channel.exchangeDeclare(topic, "fanout");
            if(queue==null){
                //订阅者模式
                queue = channel.queueDeclare().getQueue();
            }else{
                //争夺者模式
                //队列声明,默认不持久化
                channel.queueDeclare(queue, false, false, false, null);
            }
            this.queue = queue;

            //队列交换机绑定
            channel.queueBind(queue, topic, queue);
        }catch (IOException e) {
            //ignore
        }
    }

    @PreDestroy
    public void destroy(){
        try {
            if(channel!=null) {
                channel.close();
            }
            if(connection!=null) {
                connection.close();
            }
        }catch (IOException e) {
            //ignore
        }
    }

    public void listen(Consumer consumer) {

        try{
            channel.basicConsume(queue, true, consumer);
        }catch (IOException e){
            //ignore
        }
    }

}
