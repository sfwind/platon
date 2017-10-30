package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SubscribePushReceiver {
    public static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "subscribe_push_queue";
    private static final String prefix = "subscribe_push_";


    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private PlanService planService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            JSONObject msg = JSON.parseObject(message.getMessage().toString());
            String scene = msg.getString("scene");
            if (scene != null && scene.startsWith(prefix)) {
                String[] split = scene.split("_");
                Integer pushId = Integer.parseInt(split[2]);
                String openId = msg.getString("openid");
                String callback = accountService.loadSubscribePush(pushId);

                logger.info("前往问题页面:{}", scene);
                customerMessageService.sendCustomerMessage(openId, "Hi，欢迎来到圈外！\n\n" +
                        "<a href='" + callback + "'>返回刚才的页面</a>\n", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            }
        });
    }
}
