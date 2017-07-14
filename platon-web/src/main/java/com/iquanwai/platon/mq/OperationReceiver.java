package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by xfduan on 2017/7/14.
 */
public class OperationReceiver {

    public static final String TOPIC = "operation";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, OperationReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立：" + OperationReceiver.TOPIC);
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
    }

    private Consumer getConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                logger.info("receiver message {}", message);
                switch (message) {
                    case "record":

                }
            }
        };
    }

}
