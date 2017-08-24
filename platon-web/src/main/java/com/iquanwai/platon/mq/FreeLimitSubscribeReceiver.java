package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class FreeLimitSubscribeReceiver {

    public static final String TOPIC = "subscribe_quanwai";
    public static final String QUEUE = "FreeLimitEvent_Queue";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationFreeLimitService operationFreeLimitService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) -> {
            activeAction(messageQueue.getMessage().toString());
        });
    }

    private void activeAction(String message) {
        logger.info("receiver message {}", message);
        JSONObject json = JSONObject.parseObject(message);
        String scene = json.get("scene").toString();
        if (!scene.startsWith("freeLimit")) {
            logger.info(scene);
            return;
        }
        String openId = json.get("openid").toString();
        String event = json.get("event").toString();

        String[] sceneParams = scene.split("_");

        try {
            // 只记录限免小课活动
            if (sceneParams.length == 3) {
                operationFreeLimitService.recordPromotionLevel(openId, scene);
                String sendMsg;
                if (Integer.parseInt(sceneParams[2]) == ConfigUtils.getTrialProblemId()) {
                    sendMsg = "欢迎来到【圈外职场研究所】\n\n" +
                            "职场中有种能力，能让人：\n\n" +
                            "从不加班，还能不断升职\n" +
                            "秒懂他人心思、人缘爆表\n" +
                            "提案一次通关、从不修改\n\n" +
                            "你是否也拥有这种能力?\n\n" +
                            "<a href='" + ConfigUtils.domainName() + "/rise/static/eva/start'>点击开始职场洞察力检测</a>";
                } else {
                    // 非限免
                    sendMsg = "欢迎来到圈外，你刚才扫码的课程在这里，点击查看：\n" +
                            "\n" +
                            "<a href='" + ConfigUtils.adapterDomainName() +
                            "/rise/static/plan/view?id=" +
                            sceneParams[2] +
                            "'>『" + cacheService.getProblem(Integer.parseInt(sceneParams[2])).getProblem() + "』</a>\n" +
                            "\n" +
                            "完成限免小课章节有神秘卡片哦，注意收集[机智]\n" +
                            "------------\n" +
                            "P. S. 你是高洞察力的职场人吗？\n\n" +
                            "<a href='" + ConfigUtils.adapterDomainName() +
                            "/rise/static/eva/start'>『点击开始测试』</a>";
                }
                customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
