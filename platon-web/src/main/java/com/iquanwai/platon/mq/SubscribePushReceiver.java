package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.customer.PrizeCardService;
import com.iquanwai.platon.biz.domain.daily.DailyService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribePush;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class SubscribePushReceiver {
    public static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "subscribe_push_queue";
    private static final String PREFIX = "subscribe_push_";
    private static final String DAILTTALK = "daily_talk_";


    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PrizeCardService prizeCardService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private DailyService dailyService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> template = Maps.newHashMap();

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            JSONObject msg = JSON.parseObject(message.getMessage().toString());
            String scene = msg.getString("scene");
            if (scene != null && scene.startsWith(PREFIX)) {
                String[] split = scene.split("_");
                Integer pushId = Integer.parseInt(split[2]);
                String openId = msg.getString("openid");
                SubscribePush push = accountService.loadSubscribePush(pushId);
                if (push == null) {
                    logger.error("缺少push对象:{}", message);
                    return;
                }
                if(push.getScene().startsWith(DAILTTALK)){
                    dailyService.sendMsg(openId);
                } else  if (push.getScene().startsWith("prize_card_")) {
                    String[] sceneStrArr = push.getScene().split("_");
                    Profile profile = accountService.getProfile(openId);
                    if (sceneStrArr.length == 3) {
                        String cardId = sceneStrArr[2];
                        Pair<Integer, String> result = prizeCardService.isPreviewCardReceived(cardId, profile.getId());
                        OperationLog operationLog = OperationLog.create().module("礼品卡管理").function("礼品卡引流").action("领取礼品卡");
                        operationLogService.log(operationLog);
                        if (result.getLeft() == 0) {
                            logger.info("===========领取成功=======");
                            prizeCardService.sendReceivedAnnualMsgSuccessful(openId, profile.getNickname());
                        } else {
                            logger.info("===========领取失败=======");
                            if(result.getLeft() == -2){
                                prizeCardService.sendReceivedAnnualFailureMsg(openId, "抱歉，商学院体验卡已过期。如果想了解圈外商学院，请点击下方菜单“商学院”吧！");
                            }else{
                                prizeCardService.sendReceivedAnnualFailureMsg(openId, result.getRight());
                            }
                        }
                    }
                } else if (push.getScene().startsWith("annual_prize_card")) {
                    receiveAnnualPrizeCard(push.getScene(),openId);
                } else {
                    String callback = push.getCallbackUrl();
                    String templateMsg = template.get(push.getScene());
                    logger.info("前往callback页面:{}", scene);
                    customerMessageService.sendCustomerMessage(openId, templateMsg.replace("{callbackUrl}", callback), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                }
            }
        });
        initTemplate();
    }

    public void initTemplate() {
        template.put("show_word", "" +
                "Hi 欢迎来到【圈外商学院|一期一会】\n\n" +
                "<a href='{callbackUrl}'>查看答案文稿</a>");
        template.put("annual",
                "<a href='{callbackUrl}'>点击查看他的年终回顾并领取礼品卡</a>");
    }

    /**
     * 领取年度礼品卡
     * @param scene
     * @param openId
     */
    private void receiveAnnualPrizeCard(String scene,String openId){
        String[] sceneStrArr = scene.split("_");
        Profile profile = accountService.getProfile(openId);
        if (sceneStrArr.length == 4) {
            String cardId = sceneStrArr[3];
            String result = prizeCardService.receiveAnnualPrizeCards(cardId, profile.getId());
            OperationLog operationLog = OperationLog.create().module("礼品卡管理").function("年度礼品卡引流").action("领取年度礼品卡");
            operationLogService.log(operationLog);
            if ("领取成功".equals(result)) {
                logger.info("===========领取成功=======");
                prizeCardService.sendReceivedAnnualMsgSuccessful(openId, profile.getNickname());
            } else {
                prizeCardService.sendReceivedAnnualFailureMsg(openId,result);
                logger.info("===========领取失败=======");
            }
        }
    }



}
