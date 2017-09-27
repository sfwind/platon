package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Created by 三十文 on 2017/9/20
 * 强行开课 MQ
 */
@Service
public class ForceOpenCourseReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private PlanService planService;

    private static final String TOPIC = "monthly_camp_force_open_topic";
    private static final String QUEUE = "monthly_camp_force_open_queue";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (messageQueue) -> {
            activeAction(messageQueue.getMessage().toString());
        });
    }

    private void activeAction(String message) {
        JSONObject json = JSONObject.parseObject(message);
        Integer profileId = json.getInteger("profileId");
        Integer problemId = json.getInteger("problemId");
        Date startDate = json.getDate("startDate");
        Date closeDate = json.getDate("closeDate");

        planService.forceOpenProblem(profileId, problemId, startDate, closeDate);
    }

}
