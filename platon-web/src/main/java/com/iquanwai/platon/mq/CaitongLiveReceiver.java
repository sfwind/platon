package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.operation.LiveRedeemCodeRepository;
import com.iquanwai.platon.biz.domain.fragmentation.operation.TheatreService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.TheatreServiceImpl;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.LiveRedeemCode;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.PromotionConstants;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

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
    @Autowired
    private LiveRedeemCodeRepository liveRedeemCodeRepository;

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
                            List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(promoterId, TheatreServiceImpl.CURRENT_GAME);
                            if (promotionLevels.size() < 3) {
                                // 可以送
                                LiveRedeemCode liveRedeemCode = liveRedeemCodeRepository.useLiveRedeemCode(TheatreServiceImpl.CURRENT_GAME, profile.getId());
                                if (liveRedeemCode == null) {
                                    //TODO 兑换码耗尽

                                } else {
                                    //TODO 成功送出
                                    StringBuilder message1 = new StringBuilder("你好啊，勇士的朋友，你可以免费得到一枚直播兑换码\n")
                                            .append("兑换码：").append(liveRedeemCode.getCode()).append("\n")
                                            .append("直播地址：").append(TheatreServiceImpl.Live_URL).append("\n")
                                            .append("兑换码使用说明：").append(TheatreServiceImpl.CODE_DESCRIBE_URL).append("\n");
                                    customerMessageService.sendCustomerMessage(profile.getOpenid(), message1.toString(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                                    customerMessageService.sendCustomerMessage(profile.getOpenid(), "如果你也想自己当勇士获得神秘宝藏，那就做下方的题目开始闯关吧", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                                }
                            }
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
