package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.SubscribePush;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
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


    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;

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
                String callback = push.getCallbackUrl();
                String templateMsg = template.get(push.getScene());
                logger.info("前往问题页面:{}", scene);
                customerMessageService.sendCustomerMessage(openId, templateMsg.replace("{callbackUrl}", callback), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            }
        });
        initTemplate();
    }

    public void initTemplate() {
        template.put("show_word", "" +
                "Hi 欢迎来到【圈外商学院|一期一会】\n\n" +
                "<a href='{callbackUrl}'>查看答案文稿</a>");
    }
}
