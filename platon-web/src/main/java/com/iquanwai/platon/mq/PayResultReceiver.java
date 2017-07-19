package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by nethunder on 2017/7/19.
 */
public class PayResultReceiver {

    public static final String TOPIC = "rise_pay_success_topic";
    public static final String QUEUE = "pay_success";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationService operationService;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(QUEUE, SubscribeReceiver.TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立：" + SubscribeReceiver.TOPIC);
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
    }

    private Consumer getConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                logger.info("receiver message {}", message);
                QuanwaiOrder quanwai = JSONObject.parseObject(message, QuanwaiOrder.class);
                if (quanwai == null) {
                    logger.error("获取支付成功mq消息异常");
                } else {
                }
            }
        };
    }

}