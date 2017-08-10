package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

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
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private MQService mqService;

    public static void main(String[] args) {

    }

    @PostConstruct
    public void init(){
        rabbitMQFactory.initReceiver(QUEUE,TOPIC,(messageQueue)->{
            logger.info("receive message {}", messageQueue.getMessage().toString());
            QuanwaiOrder quanwai = (QuanwaiOrder)messageQueue.getMessage();
            logger.info("获取支付成功 message {}", quanwai);
            if (quanwai == null) {
                logger.error("获取支付成功mq消息异常");
            } else {
                operationService.recordOrderAndSendMsg(quanwai.getOpenid(), PromotionUser.PAY);
            }
        });
    }
}
