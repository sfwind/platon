package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
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
    private PictureService pictureService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private MQService mqService;
    @Autowired
    private UnionUserService unionUserService;

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
                    pictureService.reloadModule();
                    break;
                case "member":
                    // 返回当前登录人数
                    Integer memberSize = unionUserService.getAllLoginUsers().size();
                    logger.info("当前登录人数:{}", memberSize);
                    break;
                case "mqip": {
                    mqService.reload();
                    logger.info("刷新ip");
                    break;
                }
                default: {
                    logger.error("默认");
                }
            }
        });
    }

}
