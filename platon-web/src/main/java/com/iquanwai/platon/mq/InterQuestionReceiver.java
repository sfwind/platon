package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionServiceImpl;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class InterQuestionReceiver {

    public static final String TOPIC = "subscribe_quanwai";
    public static final String QUEUE = "InterQuestion_Queue";
    public static final String QUESTION_SUBMIT_PAGE = ConfigUtils.adapterDomainName() + "/rise/static/inter/question/submit?date=";


    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            logger.info("receiver message {}", message);
            JSONObject json = JSONObject.parseObject(message);
            String scene = json.get("scene").toString();
            if (!scene.startsWith(InterlocutionServiceImpl.QUESTION_SUBMIT_PAGE)) {
                return;
            } else {
                logger.info("前往问题页面:{}", scene);
                String openId = json.get("openId").toString();
                String[] sceneParams = scene.split("_");
                String date = sceneParams[2];
                customerMessageService.sendCustomerMessage(openId, "Hi，欢迎来到【圈外商学院|一期一会】\n\n" +
                        "<a href='" + QUESTION_SUBMIT_PAGE + date + "'>向圈圈提问</a>\n", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            }
        });
    }

}
