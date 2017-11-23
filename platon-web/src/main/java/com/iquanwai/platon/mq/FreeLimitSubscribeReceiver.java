package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
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
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) -> {
            activeAction(messageQueue.getMessage().toString());
        });
    }

    private void sendScanMsg(String openId) {
        String message = "不好，来迟了一步，洞察力测评都关闭了。\n\n" +
                "然而，课程是开放滴，和小伙伴们一起升级职场洞察力？戳这里：\n\n" +
                "\uD83D\uDC49<a href='" + ConfigUtils.domainName() + "/pay/static/audition/success?choose=true'>点击领取试听小课</a>";
        customerMessageService.sendCustomerMessage(openId, message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
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
                sendScanMsg(openId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
