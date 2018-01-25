package com.iquanwai.platon.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.operation.LiveRedeemCodeRepository;
import com.iquanwai.platon.biz.domain.fragmentation.operation.TheatreService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CardRepository;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
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
    @Autowired
    private LiveRedeemCodeRepository liveRedeemCodeRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UploadResourceService uploadResourceService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, mq -> {
            SubscribeEvent subscribeEvent = JSON.parseObject(JSON.toJSONString(mq.getMessage()), SubscribeEvent.class);
            logger.info("receive message {}", subscribeEvent);
            if (StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.CAITONG_LIVE)) {
                String scene = subscribeEvent.getScene();
                String[] sceneWords = scene.split("_");
                if (sceneWords.length < 2) {
                    logger.error("采铜直播扫码异常:{}", subscribeEvent);
                    return;
                }
                // 采铜直播
                logger.info("采铜直播二维码扫描:{}", mq);
                Profile profile = accountService.getProfile(subscribeEvent.getOpenid());
                PromotionLevel existLevel = promotionLevelDao.loadByProfileId(profile.getId(), PromotionConstants.Activities.CAITONG_LIVE);
                boolean notExistLevel = null == existLevel;
                Integer level = null;
                if (notExistLevel) {
                    // 第一次参加活动,加入level表
                    PromotionLevel tempLevelToSave = new PromotionLevel();
                    tempLevelToSave.setValid(1);
                    tempLevelToSave.setActivity(PromotionConstants.Activities.CAITONG_LIVE);
                    tempLevelToSave.setProfileId(profile.getId());
                    // 查看是第几层
                    Integer promoterId = null;
                    if (StringUtils.isNumeric(sceneWords[1])) {
                        promoterId = Integer.parseInt(sceneWords[1]);
                        if (promoterId == profile.getId()) {
                            // 自己扫描自己
                            level = 1;
                        } else {
                            PromotionLevel promoterLevel = promotionLevelDao.loadByProfileId(promoterId, PromotionConstants.Activities.CAITONG_LIVE);
                            level = promoterLevel.getLevel() + 1;
//                            List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(promoterId, TheatreServiceImpl.CURRENT_GAME);
//                            if (promotionLevels.size() < 3) {
//                                // 可以送
//                                LiveRedeemCode liveRedeemCode = liveRedeemCodeRepository.useLiveRedeemCode(TheatreServiceImpl.CURRENT_GAME, profile.getId());
//                                if (liveRedeemCode == null) {
//                                    //TODO 兑换码耗尽
//                                    logger.error("兑换码耗尽");
//                                } else {
//                                    theatreService.sendCodeToUser(profile, liveRedeemCode);
//                                    customerMessageService.sendCustomerMessage(profile.getOpenId(), "如果你也想自己当勇士获得神秘宝藏，那就做回复【48】开始闯关吧", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
//                                }
//                            } else {
//                                String message = "很抱歉，你朋友的奖励已经被大家抢光了。但是你可以选择回复【48】自己当勇士找到神秘宝藏。";
//                                customerMessageService.sendCustomerMessage(profile.getOpenId(), message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
//                            }
                        }
                    } else {
                        // 第一层
                        level = 1;
                    }
                    tempLevelToSave.setPromoterId(promoterId);
                    tempLevelToSave.setLevel(level);
                    promotionLevelDao.insertPromotionLevel(tempLevelToSave);
                } else {
                    level = existLevel.getLevel();
                }

                // 发送直播链接
                theatreService.sendLiveCode(profile.getOpenid());


                // 扫码action
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setActivity(PromotionConstants.Activities.CAITONG_LIVE);
                promotionActivity.setAction(PromotionConstants.CaitongLiveAction.SCAN_CODE);
                promotionActivityDao.insertPromotionActivity(promotionActivity);
                // 开始玩游戏
                // TODO 暂时停止游戏
//                PromotionActivity manualStart = promotionActivityDao.loadAction(profile.getId(), TheatreServiceImpl.CURRENT_GAME, TheatreServiceImpl.CURRENT_ACTION.MANUAL_START);
//                if (level == 1 || manualStart != null) {
//                    theatreService.startGame(profile);
//                }

            }
        });
    }


}
