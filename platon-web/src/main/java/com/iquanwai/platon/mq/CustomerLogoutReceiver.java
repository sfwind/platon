package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.web.resolver.UnionUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CustomerLogoutReceiver {

    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    public static final String TOPIC = "customer_logout";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, mqDto -> {
            String message = mqDto.getMessage().toString();
            logger.info("receive message {}", message);
            unionUserService.logout(message);
        });
    }
}

