package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.operation.CourseReductionService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    private OperationEvaluateService operationEvaluateService;
    @Autowired
    private AccountService accountService;
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
                Profile profile = accountService.getProfile(quanwaiOrder.getOpenid());
                Assert.notNull(profile, "付费用户不能为空");
                operationEvaluateService.recordPayAction(profile.getId());
                // 优惠推广活动
                courseReductionService.saveCourseReductionPayedLog(quanwaiOrder);
            }
        });
    }
}
