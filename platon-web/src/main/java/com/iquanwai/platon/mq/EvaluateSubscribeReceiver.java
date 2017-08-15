package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
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
    private CacheService cacheService;
    @Autowired
    private OperationEvaluateService operationEvaluateService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "EvaluateEvent_Queue";

    private static String SUBSCRIBE = "subscribe";
    private static String SCAN = "scan";

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
        String event = json.getString("event");

        String[] sceneParams = scene.split("_");
        Assert.isTrue(sceneParams.length == 3, "场景值错误：" + scene);
        if (!sceneParams[0].equalsIgnoreCase(PromotionConstants.Activities.Evaluate)) return;

        // 扫码上面，码上的数据
        String source = sceneParams[1];
        Integer problemId = Integer.parseInt(sceneParams[2]);

        Profile profile = accountService.getProfile(openId);
        Assert.notNull(profile, "扫码用户不能为空");

        // 记录扫码事件
        operationEvaluateService.recordScan(profile.getId(), source);

        // 显示限免数据
        Integer freeProblemId = ConfigUtils.getTrialProblemId();

        if (problemId.equals(freeProblemId)) {
            // 限免
            boolean isRiseMember = accountService.isRiseMember(profile.getId());

            if (isRiseMember) {
                sendMsgOne(profile.getOpenid());
            } else {
                ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profile.getId(), freeProblemId);
                if (improvementPlan == null) {
                    // 没有学习过
                    sendMsgTwo(openId);
                } else {
                    // 已经学习过
                    sendMsgOne(openId);
                }
            }
        } else {
            // 非限免
            sendMsgThree(openId);
        }
    }

    private void sendMsgOne(String openId) {
        logger.info("send msg one");
        String message = "send msg one";
        customerMessageService.sendCustomerMessage(openId, message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    private void sendMsgTwo(String openId) {
        logger.info("send msg two");
        String message = "send msg two";
        customerMessageService.sendCustomerMessage(openId, message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    private void sendMsgThree(String openId) {
        logger.info("send msg three");
        String message = "send msg three";
        customerMessageService.sendCustomerMessage(openId, message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}

