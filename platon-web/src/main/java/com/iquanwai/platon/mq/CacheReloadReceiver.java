package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.common.member.RiseMemberTypeRepo;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.platon.job.RiseMemberJob;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
    private RiseMemberTypeRepo riseMemberTypeRepo;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private RiseMemberJob riseMemberJob;

    @PostConstruct
    public void init(){
        logger.info("---------test:{}", System.getProperty("es.set.netty.runtime.available.processors"));
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立");
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
        logger.info("开启队列监听");
    }


    private Consumer getConsumer(Channel channel){
        return new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                logger.info("receive message {}", message);
                switch (message){
                    case "region":
                        accountService.reloadRegion();
                        break;
                    case "reload":
                        cacheService.reload();
                        pictureService.reloadModule();
                        break;
                    case "member":
                        riseMemberJob.refreshStatus();
                        break;
                }
            }
        };
    }
}
