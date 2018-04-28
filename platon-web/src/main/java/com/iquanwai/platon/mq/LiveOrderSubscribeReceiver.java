package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.dao.common.LivesFlowDao;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.flow.LivesFlow;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by 三十文
 */
@Service
public class LiveOrderSubscribeReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private LivesFlowDao livesFlowDao;

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "LiveOrder_QUEUE";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            action(message.getMessage().toString());
        });
    }

    private void action(String message) {
        logger.info("receive message {}", message);
        JSONObject jsonObject = JSONObject.parseObject(message);

        String scene = jsonObject.getString("scene");
        if (!scene.startsWith("liveOrder")) {
            return;
        }
        String openId = jsonObject.getString("openid");
        String[] sceneParams = scene.split("_");

        Integer liveId = Integer.valueOf(sceneParams[1]);
        LivesFlow livesFlow = livesFlowDao.load(LivesFlow.class, liveId);

        String customerMessage = "Hi，欢迎来到圈外商学院。\n" +
                "本期大咖直播课，<a href='" + ConfigUtils.domainName() + "/rise/static/live/order?liveId=" + livesFlow.getId() + "'>点击领取</a>。\n";

        customerMessageService.sendCustomerMessage(openId, customerMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}
