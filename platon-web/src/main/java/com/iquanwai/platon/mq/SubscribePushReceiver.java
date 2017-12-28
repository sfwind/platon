package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.fragmentation.operation.PrizeCardService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribePush;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class SubscribePushReceiver {
    public static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "subscribe_push_queue";
    private static final String PREFIX = "subscribe_push_";


    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private PrizeCardService prizeCardService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> template = Maps.newHashMap();

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            JSONObject msg = JSON.parseObject(message.getMessage().toString());
            String scene = msg.getString("scene");
            logger.info("scene:"+scene);
            if (scene != null && scene.startsWith(PREFIX)) {
                String[] split = scene.split("_");
                Integer pushId = Integer.parseInt(split[2]);
                String openId = msg.getString("openid");
                SubscribePush push = accountService.loadSubscribePush(pushId);
                if (push == null) {
                    logger.error("缺少push对象:{}", message);
                    return;
                }
                String callback = push.getCallbackUrl();
                String templateMsg = template.get(push.getScene());
                logger.info("前往callback页面:{}", scene);
                customerMessageService.sendCustomerMessage(openId, templateMsg.replace("{callbackUrl}", callback), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            }
            else if(scene!=null && scene.startsWith("prize_card_")){
                String openId = msg.getString("openid");
                Profile profile = accountService.getProfile(openId);
                String[] sceneStrArr = scene.split("_");
                if(sceneStrArr.length == 3){
                    String cardId = sceneStrArr[2];
                    String result = prizeCardService.isPreviewCardReceived(cardId, profile.getId());
                    //TODO:OperationLog=>打点
                    if("恭喜您获得该礼品卡".equals(result)){
                        //TODO:发送成功领取的通知
                        String templeateMsg = template.get("prize_card_receive_success");
                        // SubscribePush push = accountService.loadSubscribePush(pushId);
                        //  String callback = push.getCallbackUrl();
                        logger.info("===========领取成功=======");
                        customerMessageService.sendCustomerMessage(openId,templeateMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    }else{
                        //TODO:领取失败
                        String templeateMsg = template.get("prize_card_receive_failure");
                        logger.info("===========领取失败=======");
                        customerMessageService.sendCustomerMessage(openId,templeateMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    }
                }

            }
        });
        initTemplate();
    }

    public void initTemplate() {
        template.put("show_word", "" +
                "Hi 欢迎来到【圈外商学院|一期一会】\n\n" +
                "<a href='{callbackUrl}'>查看答案文稿</a>");
        template.put("annual",
                "<a href='{callbackUrl}'>点击查看他的年终回顾并领取礼品卡</a>");
        template.put("prize_card_receive_success","你好，欢迎来到圈外商学院！\n 你成功领取");
        template.put("prize_card_receive_failure","领取失败");
    }
}
