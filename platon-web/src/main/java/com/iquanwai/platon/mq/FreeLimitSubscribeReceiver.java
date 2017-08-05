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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
public class FreeLimitSubscribeReceiver {

    public static final String TOPIC = "subscribe_quanwai";
    public static final String QUEUE = "FreeLimitEvent_Queue";

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
        receiver.init(QUEUE, TOPIC);
        logger.info("通道建立：" + TOPIC);
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        Consumer<Object> consumer = msg -> {

            String message = JSON.toJSONString(msg);
            logger.info("receiver message {}", message);
            JSONObject json = JSONObject.parseObject(message);
            String scene = json.get("scene").toString();
            if(!scene.startsWith("freeLimit")){
                logger.info(scene);
                return;
            }
            String openId = json.get("openid").toString();
            String event = json.get("event").toString();

            String[] sceneParams = scene.split("_");

            try {
                // 只记录限免小课活动
                if (sceneParams.length == 3) {
                    Problem freeProblem = cacheService.getProblem(ConfigUtils.getTrialProblemId());
                    String freeProblemName = freeProblem.getProblem();
                    operationService.recordPromotionLevel(openId, scene);
                    String sendMsg;
                    if (Integer.parseInt(sceneParams[2]) == ConfigUtils.getTrialProblemId()) {
                        // 限免课
                        if (event.equalsIgnoreCase(SUBSCRIBE)) {
                            sendMsg = "你要的限免课程在这里，名额有限，速速点击领取：\uD83D\uDC47\n" +
                                    "\n" +
                                    "<a href='" + ConfigUtils.adapterDomainName() +
                                    "/rise/static/plan/view?id=" +
                                    ConfigUtils.getTrialProblemId() +
                                    "&free=true'>『" + freeProblemName + "』</a>\n" +
                                    "------------\n" +
                                    "P. S. 完成小课章节有神秘卡片，注意收集[机智]\n" +
                                    "\n" +
                                    "这里就是上课的教室，强烈建议点击右上角置顶哦~";
                            customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                        } else if (event.equalsIgnoreCase(SCAN)) {
                            sendMsg = "你要的限免课程在这里，名额有限，速速点击领取：\uD83D\uDC47\n" +
                                    "\n" +
                                    "<a href='" + ConfigUtils.adapterDomainName() +
                                    "/rise/static/plan/view?id=" +
                                    ConfigUtils.getTrialProblemId() +
                                    "&free=true'>『" + freeProblemName + "』</a>\n" +
                                    "------------\n" +
                                    "P. S. 完成小课章节有神秘卡片，注意收集[机智]\n";
                            customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                        }
                    } else {
                        // 非限免
                        sendMsg = "你要的小课在这里，名额有限，速速点击领取：\uD83D\uDC47\n" +
                                "\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                sceneParams[2] +
                                "'>『" + cacheService.getProblem(Integer.parseInt(sceneParams[2])).getProblem() + "』</a>\n" +
                                "\n" +
                                "完成限免小课章节有神秘卡片哦，注意收集[机智]\n" +
                                "------------\n" +
                                "P. S. 正好有一门小课限免，感兴趣可以戳：\n"+
                                "\n" +
                                "<a href='" + ConfigUtils.adapterDomainName() +
                                "/rise/static/plan/view?id=" +
                                ConfigUtils.getTrialProblemId() +
                                "'>『" + freeProblemName + "』</a>";
                        customerMessageService.sendCustomerMessage(openId, sendMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        };
        receiver.listen(consumer);
    }

}
