package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
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
    private MQService mqService;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        logger.info("通道建立");
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        // 监听器
        receiver.listen(msg -> {
            String message = msg.toString();
            logger.info("receive message {}", message);
            switch (message) {
                case "region":
                    accountService.reloadRegion();
                    break;
                case "reload":
                    cacheService.reload();
                    pictureService.reloadModule();
                    break;
            }
        });
        logger.info("开启队列监听");
    }


}
