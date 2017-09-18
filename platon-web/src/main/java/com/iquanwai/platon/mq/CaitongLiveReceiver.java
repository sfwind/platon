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
                PromotionLevel existLevel = promotionLevelDao.loadByProfileId(profile.getId(), PromotionConstants.Activities.CaitongLive);
                boolean notExistLevel = null == existLevel;
                Integer level = null;
                if (notExistLevel) {
                    // 第一次参加活动,加入level表
                    PromotionLevel tempLevelToSave = new PromotionLevel();
                    tempLevelToSave.setValid(1);
                    tempLevelToSave.setActivity(PromotionConstants.Activities.CaitongLive);
                    tempLevelToSave.setProfileId(profile.getId());
                    // 查看是第几层
                    Integer promoterId = null;
                    if (StringUtils.isNumeric(sceneWords[1])) {
                        promoterId = Integer.parseInt(sceneWords[1]);
                        if (promoterId == profile.getId()) {
                            // 自己扫描自己
                            level = 1;
                        } else {
                            PromotionLevel promoterLevel = promotionLevelDao.loadByProfileId(promoterId, PromotionConstants.Activities.CaitongLive);
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
//                                    customerMessageService.sendCustomerMessage(profile.getOpenid(), "如果你也想自己当勇士获得神秘宝藏，那就做回复【48】开始闯关吧", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
//                                }
//                            } else {
//                                String message = "很抱歉，你朋友的奖励已经被大家抢光了。但是你可以选择回复【48】自己当勇士找到神秘宝藏。";
//                                customerMessageService.sendCustomerMessage(profile.getOpenid(), message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
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

                // 直接送兑换码
                LiveRedeemCode liveRedeemCode = liveRedeemCodeRepository.useLiveRedeemCode(TheatreServiceImpl.CURRENT_GAME, profile.getId());
                if (liveRedeemCode == null) {
                    //TODO 兑换码耗尽
                    logger.error("兑换码耗尽");
                } else {
                    String msg1 = "【大咖直播限时免费】\n" +
                            "\n" +
                            "昨天很多同学已经猜到那个神秘的男子是采铜老师啦。没错，我们邀请到了畅销书《精进》作者、浙大心理学博士采铜老师来为大家做直播分享。\n" +
                            "\n" +
                            "直播原价88元，出关期间限免，使用下方兑换码，可以免费报名。\n" +
                            "-------------------\n" +
                            "报名步骤：\n" +
                            "1. 长按复制下方兑换码\n" +
                            "2. 点击链接\n" +
                            "3. 选择用微信登录（无需下载APP）\n" +
                            "4. 输入兑换码\n" +
                            "5. 访问查看课程\n" +
                            "-------------------\n" +
                            "如需帮助，可以回复【兑换码】查看兑换说明。\n" +
                            "\n" +
                            "↓兑换码↓（长按复制）";
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), msg1, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), liveRedeemCode.getCode(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    String msg2 = "< a href='http://m.study.163.com/myCoupon'>长按复制上方兑换码，点我兑换</ a>";
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), msg2, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                }


                // 扫码action
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setActivity(PromotionConstants.Activities.CaitongLive);
                promotionActivity.setAction(PromotionConstants.CaitongLiveAction.ScanCode);
                promotionActivityDao.insertPromotionActivity(promotionActivity);
                // 开始玩游戏
                // TODO 暂时停止游戏
//                PromotionActivity manualStart = promotionActivityDao.loadAction(profile.getId(), TheatreServiceImpl.CURRENT_GAME, TheatreServiceImpl.CURRENT_ACTION.ManualStart);
//                if (level == 1 || manualStart != null) {
//                    theatreService.startGame(profile);
//                }
            }
        });
    }


}
