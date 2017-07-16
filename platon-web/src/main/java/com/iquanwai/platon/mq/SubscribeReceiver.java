package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class SubscribeReceiver {

    public static final String TOPIC = "subscribe_quanwai";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationService operationService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, SubscribeReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立：" + SubscribeReceiver.TOPIC);
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
                JSONObject json = JSONObject.parseObject(message);
                String scene = json.get("scene").toString();
                logger.info("scene: {}", scene);
                String openId = json.get("openid").toString();
                logger.info("openId: {}", openId);
                operationService.recordPromotionLevel(openId, scene);
            }
        };
    }

}
