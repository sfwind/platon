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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by 三十文 on 2017/10/18
 */
@Service
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
            replyContent = "恭喜您成功领取踢踢老师直播课\n" +
                    "\n" +
                    "报名密码：963258\n" +
                    "\n" +
                    "\uD83D\uDC49<a href='https://m.qlchat.com/topic/2000000114861380.htm?shareKey=6957a5d4553d45d8e5c509f3c79d212c'>戳我报名</a>\uD83D\uDC48\n" +
                    "\n" +
                    "童鞋，拿了链接记得帮小哥哥宣传哦[机智]";
        } else {
            replyContent = "踢踢老师直播报名链接在此~\n" +
                    "\n" +
                    "报名密码：963258\n" +
                    "\n" +
                    "\uD83D\uDC49<a href='https://m.qlchat.com/topic/2000000114861380.htm?shareKey=6957a5d4553d45d8e5c509f3c79d212c'>戳我报名</a>\uD83D\uDC48\n" +
                    "\n" +
                    "童鞋，拿了链接记得帮小哥哥宣传哦[机智]";
        }
        customerMessageService.sendCustomerMessage(openId, replyContent, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}
