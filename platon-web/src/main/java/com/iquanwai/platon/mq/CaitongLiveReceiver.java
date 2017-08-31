package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.operation.TheatreService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.PromotionConstants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by nethunder on 2017/8/31.
 */
@Service
public class CaitongLiveReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "CaitongLive_Queue";
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TheatreService theatreService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, mq -> {
            SubscribeEvent subscribeEvent = JSON.parseObject(JSON.toJSONString(mq.getMessage()), SubscribeEvent.class);
            logger.info("receive message {}", subscribeEvent);
            if (StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.CaitongLive)) {
                String scene = subscribeEvent.getScene();
                String[] sceneWords = scene.split("_");
                if (sceneWords.length < 2) {
                    logger.error("采铜直播扫码异常:{}", subscribeEvent);
                    return;
                }
                // 采铜直播
                logger.info("采铜直播二维码扫描:{}", mq);
                Profile profile = accountService.getProfile(subscribeEvent.getOpenid());
                boolean notExistLevel = null == promotionLevelDao.loadByProfileId(profile.getId(), PromotionConstants.Activities.CaitongLive);
                if (notExistLevel) {
                    // 第一次参加活动,加入level表
                    PromotionLevel tempLevelToSave = new PromotionLevel();
                    tempLevelToSave.setValid(1);
                    tempLevelToSave.setActivity(PromotionConstants.Activities.CaitongLive);
                    tempLevelToSave.setProfileId(profile.getId());
                    // 查看是第几层
                    Integer promoterId = null;
                    Integer level = null;
                    if (StringUtils.isNumeric(sceneWords[1])) {
                        promoterId = Integer.parseInt(sceneWords[1]);
                        if (promoterId == profile.getId()) {
                            // 自己扫描自己
                            level = 1;
                        } else {
                            PromotionLevel promoterLevel = promotionLevelDao.loadByProfileId(promoterId, PromotionConstants.Activities.CaitongLive);
                            level = promoterLevel.getLevel() + 1;
                        }

                    } else {
                        // 第一层
                        level = 1;
                    }
                    tempLevelToSave.setPromoterId(promoterId);
                    tempLevelToSave.setLevel(level);
                    promotionLevelDao.insertPromotionLevel(tempLevelToSave);
                }

                // 扫码action
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setActivity(PromotionConstants.Activities.CaitongLive);
                promotionActivity.setAction(PromotionConstants.CaitongLiveAction.ScanCode);
                promotionActivityDao.insertPromotionActivity(promotionActivity);

                // 开始玩游戏
                theatreService.startGame(profile);
            }
        });
    }


}
