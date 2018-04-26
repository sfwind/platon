package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.personal.SchoolFriendService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.web.resolver.UnionUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by justin on 17/4/25.
 */
@Service
public class CacheReloadReceiver {
    public final static String TOPIC = "rise_resource_reload";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private MQService mqService;
    @Autowired
    private SchoolFriendService schoolFriendService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            logger.info("receive message {}", message);
            switch (message) {
                case "region":
                    accountService.reloadRegion();
                    break;
                case "reload":
                    cacheService.reload();
                    break;
                case "mqip": {
                    mqService.reload();
                    logger.info("刷新ip");
                    break;
                }
                case "school_friend":{
                    schoolFriendService.reload();
                    logger.info("刷新校友录");
                    break;
                }
                default: {
                    logger.error("默认");
                }
            }
        });
    }

}
