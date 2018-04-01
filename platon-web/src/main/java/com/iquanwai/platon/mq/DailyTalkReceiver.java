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

@Component
public class DailyTalkReceiver {

    public static final String TOPIC = "daily_talk_quanwai";
    private static final String QUEUE = "daily_talk_queue";


    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            logger.info("进入dailyTalk");
            JSONObject msg = JSON.parseObject(message.getMessage().toString());
        });
    }
}
