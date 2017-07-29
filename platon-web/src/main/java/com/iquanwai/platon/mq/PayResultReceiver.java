package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

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

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(PayResultReceiver.QUEUE, PayResultReceiver.TOPIC);
        logger.info("通道建立：{}", PayResultReceiver.TOPIC);
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        Consumer<Object> consumer = body -> {
            String message = JSON.toJSONString(body);
            logger.info("获取支付成功 message {}", message);
            QuanwaiOrder quanwai = JSONObject.parseObject(message, QuanwaiOrder.class);
            if (quanwai == null) {
                logger.error("获取支付成功mq消息异常");
            } else {
                operationService.recordOrderAndSendMsg(quanwai.getOpenid(), PromotionUser.PAY);
            }
        };
        receiver.listen(consumer);
    }


}
