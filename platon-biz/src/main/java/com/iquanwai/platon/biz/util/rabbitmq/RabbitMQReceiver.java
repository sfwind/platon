package com.iquanwai.platon.biz.util.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by justin on 17/1/19.
 */
public class RabbitMQReceiver {
    private Connection connection;
    @Getter
    private Channel channel;
    private String queue;
    private int port = 5672;
    @Setter
    private Consumer<RabbitMQDto> afterDealQueue;

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
            channel.queueBind(queue, topic, "");

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

    public void listen(Consumer<Object> consumer) {
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                RabbitMQDto messageQueue = JSONObject.parseObject(body, RabbitMQDto.class);
                consumer.accept(messageQueue.getMessage());
                if (afterDealQueue != null) {
                    afterDealQueue.accept(messageQueue);
                }
            }
        };

        try{
            channel.basicConsume(queue, true, defaultConsumer);
        }catch (IOException e){
            //ignore
        }
    }

}
