package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.platon.mq.CacheReloadReceiver;
import com.iquanwai.platon.mq.MonthlyCampReloadReceiver;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Map;

/**
 * Created by justin on 17/1/1.
 */
@RestController
@RequestMapping("/rise/cache")
@Api(description = "缓存刷新api")
public class CacheController {
    private RabbitMQPublisher rabbitMQPublisher;
    private RabbitMQPublisher monthlyCampRabbitMQPublisher;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RabbitMQFactory rabbitMQFactory;


    @PostConstruct
    public void init() {
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(CacheReloadReceiver.TOPIC);
        monthlyCampRabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(MonthlyCampReloadReceiver.TOPIC);
    }

    @RequestMapping(value = "/reload", method = RequestMethod.GET)
    @ApiOperation("刷新课程数据")
    public ResponseEntity<Map<String, Object>> reload() {
        try {
            rabbitMQPublisher.publish("reload");
            return WebUtils.success();
        } catch (Exception e) {
            logger.error("reload cache", e);
        }
        return WebUtils.error("reload cache");
    }

    @RequestMapping(value = "/reload/region", method = RequestMethod.GET)
    @ApiOperation("刷新地区信息")
    public ResponseEntity<Map<String, Object>> reloadRegion() {
        try {
            rabbitMQPublisher.publish("region");
            return WebUtils.success();
        } catch (Exception e) {
            logger.error("reload region", e);
        }
        return WebUtils.error("reload region");
    }

    @RequestMapping(value = "/reload/camp", method = RequestMethod.GET)
    @ApiOperation("刷新专项课售卖配置")
    public ResponseEntity<Map<String, Object>> reloadMonthlyCampConfig() {
        try {
            monthlyCampRabbitMQPublisher.publish("PurchaseConfigReload");
            return WebUtils.success();
        } catch (Exception e) {
            logger.error("reload member", e);
        }
        return WebUtils.error("reload config error");
    }

    @RequestMapping(value = "/reload/school/friend", method = RequestMethod.GET)
    @ApiOperation("刷新校友录")
    public ResponseEntity<Map<String,Object>> reloadSchoolFriend(){
        try {
            rabbitMQPublisher.publish("school_friend");
            return WebUtils.success();
        } catch (ConnectException e) {
            logger.error("reload school friend",e);
        }
            return WebUtils.error("reload school friend error");
    }


}
