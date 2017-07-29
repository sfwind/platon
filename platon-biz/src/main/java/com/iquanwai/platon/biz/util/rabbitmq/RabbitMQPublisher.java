package com.iquanwai.platon.biz.util.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.common.message.MQSendLog;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Created by justin on 17/1/19.
 */
public class RabbitMQPublisher {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String topic;
    private RabbitMQConnection rabbitMQConnection;
    private Channel channel;

    @Setter
    private Consumer<MQSendLog> sendCallback;

    public void init(String topic) {
        Assert.notNull(topic, "消息主题不能为空");
        destroy();
        this.topic = topic;

        try {
            rabbitMQConnection = RabbitMQConnection.create();
            Connection connection = rabbitMQConnection.getConnection();
            if (connection == null) {
                rabbitMQConnection.init();
                connection = rabbitMQConnection.getConnection();
            }
            if (connection == null) {
                logger.error("connection error");
                return;
            }
            channel = connection.createChannel();
            //交换机声明,广播形式
            channel.exchangeDeclare(topic, "fanout");
        } catch (IOException e) {
            logger.error("connection error", e);
        }
    }

    public void destroy() {
        try {
            if (channel != null) {
                channel.close();
            }
            if( rabbitMQConnection!=null){
                rabbitMQConnection.destroy();
            }
        } catch (IOException e) {
            logger.error("connection error", e);
        } catch (TimeoutException e) {
            logger.error("connection timeout", e);
        }
    }

    public <T> void publish(T message) throws ConnectException {
        //重连尝试
        if (channel == null) {
            init(topic);
        }
        if (channel == null) {
            throw new ConnectException();
        }

        String msgId = CommonUtils.randomString(32);

        RabbitMQDto dto = new RabbitMQDto();
        dto.setMsgId(msgId);
        dto.setMessage(message);
        String json = JSON.toJSONString(dto);
        try {
            channel.basicPublish(topic, "", null, json.getBytes());
            if (this.sendCallback != null) {
                MQSendLog mqSendLog = new MQSendLog();
                mqSendLog.setTopic(topic);
                mqSendLog.setMsgId(msgId);
                mqSendLog.setMessage(message instanceof String ? message.toString() : JSON.toJSONString(message));
                this.sendCallback.accept(mqSendLog);
            }
            logger.info("发送mq,topic:{},msgId:{},message:{}", topic, msgId, message);
        } catch (IOException e) {
            logger.error("发送mq失败", e);
        }
    }
}
