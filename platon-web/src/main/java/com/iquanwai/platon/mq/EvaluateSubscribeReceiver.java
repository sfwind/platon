package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.PromotionConstants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

@Service
public class EvaluateSubscribeReceiver {

    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationEvaluateService operationEvaluateService;
    @Autowired
    private CustomerMessageService customerMessageService;

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "EvaluateEvent_Queue";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) ->
                activeAction(messageQueue.getMessage().toString())
        );
    }

    private void activeAction(String message) {
        logger.info("receive message {}", message);
        JSONObject json = JSONObject.parseObject(message);

        String scene = json.get("scene").toString();
        if (!scene.startsWith(PromotionConstants.Activities.Evaluate)) {
            logger.info("scene: {}", scene);
            return;
        }

        String openId = json.getString("openid");

        String[] sceneParams = scene.split("_");
        Assert.isTrue(sceneParams.length == 3, "场景值错误：" + scene);
        if (!sceneParams[0].equalsIgnoreCase(PromotionConstants.Activities.Evaluate)) return;

        // 扫码上面，码上的数据
        String source = sceneParams[1];

        Profile profile = accountService.getProfile(openId);
        Assert.notNull(profile, "扫码用户不能为空");

        // 记录扫码事件
        operationEvaluateService.recordScan(profile.getId(), source);

        sendScanMsg(openId);
    }

    private void sendScanMsg(String openId) {
        String message = "欢迎来到【圈外职场研究所】\n\n" +
                "职场中有种能力，能让人：\n\n" +
                "从不加班，还能不断升职\n" +
                "秒懂他人心思、人缘爆表\n" +
                "提案一次通关、从不修改\n\n" +
                "你是否也拥有这种能力?\n\n" +
                "<a href='" + ConfigUtils.domainName() + "/rise/static/eva/start'>点击开始职场洞察力检测</a>";
        customerMessageService.sendCustomerMessage(openId, message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}

