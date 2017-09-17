package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.operation.TheatreService;
import com.iquanwai.platon.biz.po.common.WechatMessage;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by nethunder on 2017/8/30.
 */
@Service
public class WechatMessageReceiver {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String WECHAT_MESSAGE_TOPIC = "wechat_message_reply";

    @Autowired
    private TheatreService theatreService;

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, WECHAT_MESSAGE_TOPIC, (messageQueue) -> {
            WechatMessage wechatMessage = JSON.parseObject(JSON.toJSONString(messageQueue.getMessage()), WechatMessage.class);
            logger.info("receive message :{}", wechatMessage);
            // 扫过二维码，并且没有结束游戏
            Boolean isPlaying = theatreService.isPlayingTheatre(wechatMessage);
            if (isPlaying) {
                theatreService.handleTheatreMessage(wechatMessage);
            }
        });
    }
}
