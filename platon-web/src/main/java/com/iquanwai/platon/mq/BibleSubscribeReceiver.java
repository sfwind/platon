package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.PromotionConstants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * Created by nethunder on 2017/9/7.
 */
@Service
public class BibleSubscribeReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;

    private static final String TOPIC = "subscribe_quanwai";
    private static final String QUEUE = "BibleEvent_Queue";


    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, (message) -> {
            logger.info("receive message {}", message);
            JSONObject json = JSONObject.parseObject(message.getMessage().toString());

            String scene = json.get("scene").toString();
            if (!scene.startsWith(PromotionConstants.Activities.Bible)) {
                logger.info("scene: {}", scene);
                return;
            }

            String openId = json.getString("openid");
            String[] sceneParams = scene.split("_");
            Profile profile = accountService.getProfile(openId);
            Assert.notNull(profile, "扫码用户不能为空");
            Assert.isTrue(StringUtils.isNumeric(sceneParams[2]), "层数必须为数字");
            Integer level = Integer.valueOf(sceneParams[2]);
            Integer promoter = null;
            if (StringUtils.isNumeric(sceneParams[1])) {
                promoter = Integer.valueOf(sceneParams[1]);
            }
            PromotionLevel oldLevel = promotionLevelDao.loadByProfileId(profile.getId(), PromotionConstants.Activities.Bible);
            if (oldLevel == null) {
                // 开始处理
                PromotionLevel promotionLevel = new PromotionLevel();
                promotionLevel.setProfileId(profile.getId());
                promotionLevel.setActivity(PromotionConstants.Activities.Bible);
                promotionLevel.setLevel(level);
                promotionLevel.setValid(1);
                promotionLevel.setPromoterId(promoter);
                promotionLevelDao.insertPromotionLevel(promotionLevel);
            }
            // 已经扫过 ignore
            PromotionActivity promotionActivity = new PromotionActivity();
            promotionActivity.setProfileId(profile.getId());
            promotionActivity.setActivity(PromotionConstants.Activities.Bible);
            promotionActivity.setAction(PromotionConstants.BibleAction.ScanCode);
            promotionActivityDao.insertPromotionActivity(promotionActivity);
        });

    }
}
