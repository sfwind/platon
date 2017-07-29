package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
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
    private MQService mqService;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC);
        logger.info("{} 通道建立",TOPIC);
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        // 监听器
        receiver.listen(msg -> {
            String message = msg.toString();
            logger.info("receive message {}", message);
            loginUserService.updateWeixinUser(message);
        });
        logger.info("{} 开启队列监听",TOPIC);
    }
}
