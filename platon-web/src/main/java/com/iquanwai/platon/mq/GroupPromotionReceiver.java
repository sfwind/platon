package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.operation.GroupPromotionService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GroupPromotionReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private GroupPromotionService groupPromotionService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "group_promotion_queue";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            logger.info("receive message {}", message);

            JSONObject json = JSONObject.parseObject(message.getMessage().toString());
            String scene = json.getString("scene");
            if (!scene.startsWith("groupPromotion")) {
                return;
            }

            String openId = json.getString("openid");
            String[] sceneArray = scene.split("_");
            String groupCode = sceneArray[1];

            boolean participateResult = groupPromotionService.participateGroup(openId, groupCode);
            if (participateResult) {
                logger.info("{} 入团成功", openId);
            } else {
                logger.info("{} 入团失败", openId);
            }
        });
    }

}
