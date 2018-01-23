package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.web.resolver.LoginUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by justin on 17/7/25.
 */
@Service
public class LoginUserUpdateReceiver {
    @Autowired
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String TOPIC = "login_user_reload";

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            logger.info("receive message {}", messageQueue.getMessage().toString());
            loginUserService.updateLoginUserByUnionId(messageQueue.getMessage().toString());
        });
    }
}
