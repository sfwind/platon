package com.iquanwai.platon.biz.util.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.common.message.MQSendLog;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.net.ConnectException;

/**
 * Created by nethunder on 2017/8/8.
 */
@Repository
public class RabbitMQFactory {
    private Logger logger = LoggerFactory.getLogger(RabbitMQFactory.class);
    @Autowired
    private MQService mqService;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 创建广播返送者
     *
     * @param topic 交换机名称
     * @return 发送者
     */
    public RabbitMQPublisher initFanoutPublisher(String topic) {
        Assert.notNull(topic, "交换机名字不能为null");
        FanoutExchange fanoutExchange = new FanoutExchange(topic, false, false);
        amqpAdmin.declareExchange(fanoutExchange);

        return new RabbitMQPublisher() {
            @Override
            public <T> void publish(T message) throws ConnectException {
                MQSendLog mqSendLog = new MQSendLog();
                mqSendLog.setSendError(false);
                String msgId = CommonUtils.randomString(32);
                RabbitMQDto dto = new RabbitMQDto();
                dto.setMsgId(msgId);
                dto.setMessage(message);
                try {
                    amqpTemplate.send(topic, null, MessageBuilder.withBody(JSON.toJSONString(dto).getBytes()).build());
                    logger.info("发送mq,topic:{},msgId:{},message:{}", topic, msgId, message);
                } catch (Exception e) {
                    logger.error("发送mq失败", e);
                    mqSendLog.setSendError(true);
                }
                mqSendLog.setTopic(topic);
                mqSendLog.setMsgId(msgId);
                mqSendLog.setMessage(message instanceof String ? message.toString() : JSON.toJSONString(message));
                mqService.saveMQSendOperation(mqSendLog);
            }
        };
    }
}