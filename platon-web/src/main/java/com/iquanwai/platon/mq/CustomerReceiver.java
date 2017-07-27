package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.platon.web.resolver.LoginUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;


/**
 * Created by xfduan on 2017/6/30.
 */
@Service
public class CustomerReceiver {

    public static final String TOPIC = "customer";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoginUserService loginUserService;

    @Autowired
    private MQService mqService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        logger.info("通道建立：" + TOPIC);
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);

        Consumer<Object> consumer = o -> {
            String message = o.toString();
            logger.info("receive message {}", message);
            loginUserService.logout(message);
        };
        receiver.listen(consumer);
        logger.info("开启队列监听：" + TOPIC);
    }

}

