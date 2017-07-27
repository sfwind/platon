package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.Problem;
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
    private static String SCAN = "SCAN";

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
        receiver.init("FreeLimitEvent_Queue", SubscribeReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
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

            try {
                Problem freeProblem = cacheService.getProblem(ConfigUtils.getTrialProblemId());
                String freeProblemName = freeProblem.getProblem();
                operationService.recordPromotionLevel(openId, scene);

                logger.info("recordLevel, {}", openId);
                if (sceneParams.length == 3) {
                    String sendMsg;
                    logger.info("enter first");
                    if (Integer.parseInt(sceneParams[2]) == ConfigUtils.getTrialProblemId()) {
                        logger.info("限免小课");
                        // 限免课
                        if (event.equalsIgnoreCase(SUBSCRIBE)) {
                            logger.info("关注事件");
                            sendMsg = "欢迎关注【圈外同学】，你的限免课程在这里，点击上课：\n" +
                                    "\n" +
                                    "<a href='" + ConfigUtils.adapterDomainName() +
                                    "/rise/static/plan/view?id=" +
                                    ConfigUtils.getTrialProblemId() +
                                    "'>" + freeProblemName + "</a>\n" +
                                    "------------\n" +
                                    "P. S. 学习小课章节可得神秘卡片哦，分享还会获得¥50奖学金。\n" +
                                    "\n" +
                                    "点击上方课程，立即开始学习吧！";
                            customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                            logger.info("关注发玩消息");
                        } else if (event.equalsIgnoreCase(SCAN)) {
                            logger.info("扫描事件");
                            sendMsg = "你要的限免课程在这里，点击上课：\n" +
                                    "\n" +
                                    "<a href='" + ConfigUtils.adapterDomainName() +
                                    "/rise/static/plan/view?id=" +
                                    ConfigUtils.getTrialProblemId() +
                                    "'>" + freeProblemName + "</a>\n" +
                                    "------------\n" +
                                    "P. S. 学习小课章节可得神秘卡片哦，分享还会获得¥50奖学金。\n" +
                                    "\n" +
                                    "点击上方课程，立即开始学习吧！";
                            customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                            logger.info("扫描发完消息");
                        } else {
                            logger.info("其他事件 pass");
                        }
                    } else {
                        logger.info("非限免小课");
                        // 非限免
                        sendMsg = "同学你好，你要的小课在这里：\n" +
                                "\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                sceneParams[2] +
                                "'>" + cacheService.getProblem(Integer.parseInt(sceneParams[2])).getProblem() + "</a>\n" +
                                "------------\n" +
                                "P. S. 正好有一门小课限免，感兴趣可以戳：\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                ConfigUtils.getTrialProblemId() +
                                "'>" + freeProblemName + "</a>\n" +
                                "\n" +
                                "学习限免小课章节可得神秘卡片哦，分享还会获得¥50奖学金。";
                        customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                        logger.info("扫描发完消息");
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        };
        receiver.listen(consumer);
    }

}
