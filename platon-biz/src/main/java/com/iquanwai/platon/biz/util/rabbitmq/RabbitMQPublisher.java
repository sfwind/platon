package com.iquanwai.platon.biz.util.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by justin on 17/1/19.
 */
public class RabbitMQPublisher {
    private String topic;
    private Connection connection;
    private Channel channel;
    private String ipAddress;
    private int port = 5672;

    public void init(String topic, String ipAddress, Integer port){
        Assert.notNull(topic, "消息主题不能为空");
        Assert.notNull(ipAddress, "rabbit ip不能为空");
        destroy();
        this.topic = topic;
        this.ipAddress = ipAddress;
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

    public void publish(String message) throws ConnectException {
        //重连尝试
        if(connection==null || channel==null){
            init(topic, ipAddress, port);
        }
        if(channel==null){
            throw new ConnectException();
        }

        try {
            channel.basicPublish(topic, "", null, message.getBytes());
        }catch (IOException e) {
            //ignore
        }
    }

}
