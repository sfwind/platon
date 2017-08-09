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

    @RabbitListener(admin = "rabbitAdmin", bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = TOPIC, type = ExchangeTypes.FANOUT)))
    public void process(byte[] data) {
        try {
            RabbitMQDto messageQueue = JSONObject.parseObject(data, RabbitMQDto.class);
            String message = messageQueue.getMessage().toString();
            logger.info("receive message {}", message);
            loginUserService.logout(message);

            messageQueue.setTopic(TOPIC);
            messageQueue.setQueue("auto");
            mqService.updateAfterDealOperation(messageQueue);
        } catch (Exception e) {
            logger.error("mq处理异常", e);
        }
    }
}

