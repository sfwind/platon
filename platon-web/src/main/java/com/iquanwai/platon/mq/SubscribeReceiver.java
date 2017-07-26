package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
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

    private static String SUBSCRIBE = "subscribe";
    private static String SCAN = "scan";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationService operationService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CacheService cacheService;

    @Autowired
    private MQService mqService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, SubscribeReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
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
            String event = json.get("event").toString();
            logger.info("event: {}", event);
            operationService.recordPromotionLevel(openId, scene);

            String[] sceneParams = scene.split("_");
            logger.info(sceneParams[0] + " " + sceneParams[1] + " " + sceneParams[2]);
            if (sceneParams.length == 3) {
                String sendMsg;
                if (Integer.parseInt(sceneParams[2]) == ConfigUtils.getTrialProblemId()) {
                    // 限免课
                    if (event.equals(SUBSCRIBE)) {
                        sendMsg = "欢迎关注【圈外同学】，你的限免课程在这里，点击上课：\n" +
                                "------------------------------------------------\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                ConfigUtils.getTrialProblemId() +
                                "'>找到本质问题，解决无效努力</a>\n" +
                                "\n" +
                                "————————————\n" +
                                "P. S. 完成小课章节有神秘卡片哦，分享还会获得¥50奖学金。\n" +
                                "\n" +
                                "点击上方课程，立即开始学习吧！";
                        logger.info(sendMsg);
                        customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    } else if (event.equals(SCAN)) {
                        sendMsg = "你要的限免课程在这里，点击上课：\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                ConfigUtils.getTrialProblemId() +
                                "'>找到本质问题，解决无效努力</a>\n" +
                                "————————————\n" +
                                "P. S. 完成小课章节有神秘卡片哦，分享还会获得¥50奖学金。\n" +
                                "\n" +
                                "点击上方课程，立即开始学习吧！";
                        logger.info(sendMsg);
                        customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    }
                } else {
                    // 非限免
                    sendMsg = "同学你好，你要的小课在这里：\n" +
                            "<a href='" + ConfigUtils.adapterDomainName() +
                            "/rise/static/problem/view?id=" +
                            sceneParams[2] +
                            "'>" + cacheService.getProblem(Integer.parseInt(sceneParams[2])).getProblem() + "</a>\n" +
                            "————————————\n" +
                            "P. S. 正好有一门小课限免，感兴趣可以戳：\n" +
                            "<a href='" + ConfigUtils.adapterDomainName() +
                            "/rise/static/plan/view?id=" +
                            ConfigUtils.getTrialProblemId() +
                            "'>找到本质问题，解决无效努力</a>\n" +
                            "\n" +
                            "完成限免小课章节有神秘卡片哦，分享还会获得¥50奖学金。";
                    logger.info(sendMsg);
                    customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                }
            }
        };
        receiver.listen(consumer);
    }


}
