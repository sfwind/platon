package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.web.resolver.LoginUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


/**
 * Created by xfduan on 2017/6/30.
 */
@Service
public class CustomerReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String TOPIC = "customer";

    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            logger.info("receive message {}", message);
            loginUserService.logout(message);
        });
    }
}

