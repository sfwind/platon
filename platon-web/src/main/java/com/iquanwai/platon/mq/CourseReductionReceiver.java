package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.operation.CourseReductionService;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.PromotionConstants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by nethunder on 2017/8/16.
 */
@Service
public class CourseReductionReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "CourseReduction_Queue";

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CourseReductionService courseReductionService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (mq) -> {
            SubscribeEvent subscribeEvent = JSON.parseObject(JSON.toJSONString(mq.getMessage()), SubscribeEvent.class);
            logger.info("receive message {}", subscribeEvent);
            if (StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.CourseReduction)) {
                // 是优惠课程推广
                courseReductionService.scanCourseReductionQR(subscribeEvent);
            }
        });

    }
}
