package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.operation.CourseReductionService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.PromotionConstants;
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
    private OperationFreeLimitService operationFreeLimitService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CourseReductionService courseReductionService;

    @PostConstruct
    public void init(){
        rabbitMQFactory.initReceiver(QUEUE,TOPIC,(messageQueue)->{
            logger.info("receive message {}", messageQueue.getMessage().toString());
            QuanwaiOrder quanwaiOrder = JSON.parseObject(JSON.toJSONString(messageQueue.getMessage()), QuanwaiOrder.class);
            logger.info("获取支付成功 message {}", quanwaiOrder);
            if (quanwaiOrder == null) {
                logger.error("获取支付成功mq消息异常");
            } else {
                // 限免推广活动
                operationFreeLimitService.recordOrderAndSendMsg(quanwaiOrder.getOpenid(), PromotionConstants.FreeLimitAction.PayCourse);
                // 优惠推广活动
                courseReductionService.saveCourseReductionPayedLog(quanwaiOrder);
            }
        });
    }
}
