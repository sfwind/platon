package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class SubscribeReceiver {

    public static final String TOPIC = "subscribe_quanwai";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationService operationService;

    @Autowired
    private MQService mqService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, SubscribeReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立：" + SubscribeReceiver.TOPIC);
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        Consumer<Object> consumer = msg -> {
            String message = JSON.toJSONString(msg);
            logger.info("receiver message {}", message);
            JSONObject json = JSONObject.parseObject(message);
            String scene = json.get("scene").toString();
            logger.info("scene: {}", scene);
            String openId = json.get("openid").toString();
            logger.info("openId: {}", openId);
            operationService.recordPromotionLevel(openId, scene);
        };
        receiver.listen(consumer);
    }


}
