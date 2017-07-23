package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.platon.mq.CacheReloadReceiver;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by justin on 17/1/1.
 */
@RestController
@RequestMapping("/rise/cache")
public class CacheController {
    private RabbitMQPublisher rabbitMQPublisher;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MQService mqService;


    @PostConstruct
    public void init(){
        rabbitMQPublisher = new RabbitMQPublisher();
        rabbitMQPublisher.init(CacheReloadReceiver.TOPIC, ConfigUtils.getRabbitMQIp(),
                ConfigUtils.getRabbitMQPort());
//        rabbitMQPublisher.setSendCallback(queue -> mqService.saveMQSendOperation(queue));
    }

    @RequestMapping("/reload")
    public ResponseEntity<Map<String, Object>> reload(){
        try {
            rabbitMQPublisher.publish("reload");
            return WebUtils.success();
        }catch (Exception e){
            logger.error("reload cache", e);
        }
        return WebUtils.error("reload cache");
    }

    @RequestMapping("/reload/region")
    public ResponseEntity<Map<String, Object>> reloadRegion(){
        try {
            rabbitMQPublisher.publish("region");
            return WebUtils.success();
        }catch (Exception e){
            logger.error("reload region", e);
        }
        return WebUtils.error("reload region");
    }



    @RequestMapping("/reload/member")
    public ResponseEntity<Map<String, Object>> reloadMember(){
        try {
            rabbitMQPublisher.publish("member");
            return WebUtils.success();
        }catch (Exception e){
            logger.error("reload member", e);
        }
        return WebUtils.error("reload member");
    }
}
