package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.audition.AuditionService;
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

import javax.annotation.PostConstruct;

/**
 * Created by justin on 2017/11/23.
 */
@Service
public class AuditionSignupReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AuditionService auditionService;
    @Autowired
    private AccountService accountService;

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "audition_signup_queue";

    private static final String AUDITION_SUCCESS = "/pay/audition/success";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            logger.info("receive message {}", message);

            JSONObject json = JSONObject.parseObject(message.getMessage().toString());
            String scene = json.get("scene").toString();
            if (!scene.startsWith(PromotionConstants.Activities.AUDITION_SIGNUP)) {
                logger.info("scene: {}", scene);
                return;
            }

            String openid = json.getString("openid");
            customerMessageService.sendCustomerMessage(openid, "<a href='" + ConfigUtils.domainName() + AUDITION_SUCCESS + "'>" + "点击这里完成预约</a>",
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT);

            Profile profile = accountService.getProfile(openid);
            if (profile != null) {
                auditionService.setProfileIdForAuditionMember(openid, profile.getId());
            }
        });

    }
}
