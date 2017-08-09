package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQDto;
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
 * Created by nethunder on 2017/7/19.
 */
@Service
public class PayResultReceiver {

    public static final String TOPIC = "rise_pay_success_topic";
    public static final String QUEUE = "pay_success";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationService operationService;

    @Autowired
    private MQService mqService;

    @RabbitListener(admin = "rabbitAdmin", bindings = @QueueBinding(value = @Queue(value = QUEUE,durable = "false",exclusive = "false",autoDelete = "false"), exchange = @Exchange(value = TOPIC, type = ExchangeTypes.FANOUT)))
    public void process(byte[] data) {
        RabbitMQDto messageQueue = JSONObject.parseObject(data, RabbitMQDto.class);
        logger.info("receive message {}", messageQueue.getMessage().toString());
        String message = messageQueue.getMessage().toString();
        logger.info("获取支付成功 message {}", message);
        QuanwaiOrder quanwai = JSONObject.parseObject(message, QuanwaiOrder.class);
        if (quanwai == null) {
            logger.error("获取支付成功mq消息异常");
        } else {
            operationService.recordOrderAndSendMsg(quanwai.getOpenid(), PromotionUser.PAY);
        }
        messageQueue.setTopic(TOPIC);
        messageQueue.setQueue("auto");
        mqService.updateAfterDealOperation(messageQueue);
    }
}
