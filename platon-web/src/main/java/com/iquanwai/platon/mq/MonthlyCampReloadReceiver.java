package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MonthlyCampReloadReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public final static String TOPIC = "purchase_configuration_reload";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            if ("PurchaseConfigReload".equals(message)) {
                cacheService.reloadMonthlyCampConfig();
                logger.info("支付配置刷新成功");
            }
        });
    }

}
