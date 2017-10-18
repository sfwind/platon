package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * Created by 三十文 on 2017/10/18
 */
public class DistributeMessageReceiver {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TOPIC = "distribute_message";
    private static final String QUEUE = "distribute_queue";

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AccountService accountService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, mq -> {
            activeAction(mq.getMessage().toString());
        });
    }

    private void activeAction(String wechatMessage) {
        JSONObject jsonObject = JSONObject.parseObject(wechatMessage);
        String openId = jsonObject.getString("openid");
        String message = jsonObject.getString("message");
        Profile profile = accountService.getProfile(openId);
        String replyContent;
        if (profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
            replyContent = "会员信息";
        } else {
            replyContent = "非会员信息";
        }
        customerMessageService.sendCustomerMessage(openId, replyContent, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}
