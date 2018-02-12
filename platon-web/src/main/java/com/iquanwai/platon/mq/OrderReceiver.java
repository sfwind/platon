package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 订单相关处理，关于课程开通
 */
@Service
public class OrderReceiver {

    private static final String TOPIC = "camp_order_topic";
    private static final String QUEUE = "camp_order_queue";

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private PlanService planService;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) ->
            activeAction(messageQueue.getMessage().toString())
        );
    }

    private void activeAction(String message) {
        logger.info("receive monthly camp message: {}", message);
        planService.magicOpenCampOrder(message);
    }

}
