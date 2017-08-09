package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQDto;
import com.iquanwai.platon.web.resolver.LoginUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/7/25.
 */
@Service
public class LoginUserUpdateReceiver {
    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private MQService mqService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String TOPIC = "login_user_reload";


    @RabbitListener(admin = "rabbitAdmin", bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = TOPIC, type = ExchangeTypes.FANOUT)))
    public void process(byte[] data) {
        try {
            RabbitMQDto messageQueue = JSONObject.parseObject(data, RabbitMQDto.class);
            logger.info("receive message {}", messageQueue.getMessage().toString());
            loginUserService.updateWeixinUser(messageQueue.getMessage().toString());
            messageQueue.setTopic(TOPIC);
            messageQueue.setQueue("auto");
            mqService.updateAfterDealOperation(messageQueue);
        } catch (Exception e) {
            logger.error("mq处理异常", e);
        }
    }
}
