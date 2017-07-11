package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


/**
 * Created by xfduan on 2017/6/30.
 */
@Service
public class CustomerReceiver {

    public static final String TOPIC = "customer";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoginUserService loginUserService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        ;
        Channel channel = receiver.getChannel();
        logger.info("通道建立：" + TOPIC);
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
        logger.info("开启队列监听：" + TOPIC);
    }

    private Consumer getConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body);
                logger.info("receive message {}", message);
                loginUserService.logout(message);
            }
        };
    }
}

