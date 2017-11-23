package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
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
 * Created by justin on 2017/11/23.
 */
@Service
public class AuditionSignupReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;

    private static final String TOPIC = "audition_signup";
    private static final String QUEUE = "audition_signup_queue";

    private static final String AUDITION_SUCCESS = "/pay/static/audition/success";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            logger.info("receive message {}", message);

            JSONObject json = JSONObject.parseObject(message.getMessage().toString());

            String openId = json.getString("openid");
            customerMessageService.sendCustomerMessage(openId, "<a href='"+ConfigUtils.domainName()+AUDITION_SUCCESS+"'>"+"点击这里完成预约</a>",
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        });

    }
}
