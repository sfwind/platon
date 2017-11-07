package com.iquanwai.platon.biz.util.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.common.message.MQSendLog;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.net.ConnectException;
import java.util.function.Consumer;

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
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private RabbitListenerContainerFactory rabbitListenerContainerFactory;
    @Autowired
    private RabbitMQConverter mqConverter;

    public void initReceiver(String queueName, String topicName, Consumer<RabbitMQDto> consumer) {
        String receive = ConfigUtils.getValue("open.receive.mq");
        if (receive != null && "false".equals(receive)) {
            // 设置了开关，并且开关是false,则不接收mq消息
            return;
        }

        Queue queue;
        if (queueName == null) {
            queue = amqpAdmin.declareQueue();
        } else {
            queue = new Queue(queueName, false, false, false);
            amqpAdmin.declareQueue(queue);
        }
        FanoutExchange exchange = new FanoutExchange(topicName, false, false);
        Binding binding = BindingBuilder.bind(queue).to(exchange);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(binding);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue.getName());
        container.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    RabbitMQDto ob = JSON.parseObject(message.getBody(), RabbitMQDto.class);
                    consumer.accept(ob);
                    ob.setQueue(queue.getName());
                    ob.setTopic(topicName);
                    mqService.updateAfterDealOperation(ob);
                } catch (Exception e) {
                    logger.error("处理MQ异常,queue:" + queueName + ",topic:" + topicName, e);
                }
            }
        });
        container.start();
    }


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
                    amqpTemplate.convertAndSend(topic, null, dto);
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