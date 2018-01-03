package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.AnnualSummaryDao;
import com.iquanwai.platon.biz.dao.common.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.GroupPromotionDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PrizeCardServiceImpl implements PrizeCardService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private CustomerMessageService customerMessageService;

    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private GroupPromotionDao groupPromotionDao;
    @Autowired
    private AnnualSummaryDao annualSummaryDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static Integer ANNUAL_CARD_MAX = 5;
    private final static Integer ANNUAL_CARD_MIDDLE = 4;
    private final static Integer ANNUAL_CARD_MIN = 3;


    @Override
    public PrizeCard loadNoOwnerPrizeCard(Integer profileId) {
        List<PrizeCard> prizeCards = Lists.newArrayList();

        PrizeCard personalPrizeCard = prizeCardDao.loadPersonalPrizeCard(profileId);
        if (personalPrizeCard != null) {
            prizeCards.add(prizeCardDao.loadPersonalPrizeCard(profileId));
        }

        if (prizeCards.size() == 0) {
            redisUtil.lock("prize:noOwner", lock -> {
                PrizeCard noOwnerPrizeCard = prizeCardDao.loadNoOwnerPrizeCard();
                if (noOwnerPrizeCard != null) {
                    prizeCards.add(noOwnerPrizeCard);
                }
            });
        }

        if (prizeCards.size() == 1) {
            PrizeCard prizeCard = prizeCards.get(0);
            prizeCard.setProfileId(profileId);

            // 用户领取完卡片之后，将对应的用户信息添加上去
            prizeCardDao.updateProfileId(profileId, prizeCard.getId());
            return prizeCard;
        } else {
            return null;
        }
    }

    @Override
    public boolean exchangePrizeCard(Integer profileId, Integer prizeCardId) {
        boolean exchangeResult = false;

        PrizeCard prizeCard = prizeCardDao.load(PrizeCard.class, prizeCardId);

        if (prizeCard.getUsed()) {
            exchangeResult = true;
        } else if (profileId.equals(prizeCard.getProfileId())) {
            // 确保兑换的是自己的卡片
            Coupon coupon = new Coupon();
            Profile profile = accountService.getProfile(profileId);
            coupon.setOpenId(profile.getOpenid());
            coupon.setProfileId(profileId);
            coupon.setAmount(100);
            coupon.setUsed(0);
            coupon.setExpiredDate(DateUtils.afterDays(new Date(), 8));
            coupon.setDescription("奖品卡");
            int couponInsertResult = couponDao.insertCoupon(coupon);

            if (couponInsertResult > 0) {
                exchangeResult = prizeCardDao.updateUsedInfo(prizeCardId) > 0;
            }
        }
        return exchangeResult;
    }

    /**
     * 领取年度礼品卡
     */
    @Override
    public String receiveAnnualPrizeCards(String cardNo, Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        if (riseMembers.size() > 0) {
            logger.info("用户不在可领取范围内");
            return "优秀如你，已经早早开始了在圈外商学院的学习，请把体验的机会留给尚未体验过的小伙伴吧～";
        }
        if (groupPromotionDao.loadByProfileId(profileId) != null) {
            logger.info("用户已经参加一带二活动");
            return "你已经获得体验资格，把机会留给需要的小伙伴吧！";
        }
        if (prizeCardDao.loadReceiveAnnualCard(profileId).size() > 0) {
            logger.info("用户已经领取过一张");
            return "你已经获得体验资格，把机会留给需要的小伙伴吧！";
        }
        Profile profile = accountService.getProfile(profileId);
        if (profile == null) {
            logger.info("用户不存在");
            return "用户不存在";
        }
        //成功更新认为领取成功
        if (prizeCardDao.updateAnnualCard(cardNo, profile.getOpenid(), profileId) == 1) {
            //开课
            generatePlanService.createTeamLearningPlan(profileId);
            return "领取成功";
        } else {
            return "你晚来一步,礼品卡已被其他小伙伴领取";
        }
    }

    /**
     * 生成年终回顾的礼品卡并返回
     *
     * @param profileId
     */
    @Override
    public List<PrizeCard> generateAnnualPrizeCards(Integer profileId) {
        List<PrizeCard> prizeCards = prizeCardDao.getAnnualPrizeCards(profileId);
        //如果之前已经生成，则不再生成
        if (prizeCards.size() > 0) {
            return prizeCards;
        }
        AnnualSummary annualSummary = annualSummaryDao.loadUserAnnualSummary(profileId);
        if (annualSummary == null) {
            return Lists.newArrayList();
        }
        Double percent = annualSummary.getDefeatPercentage();

        if (percent >= 0.8) {
            for (int i = 0; i < ANNUAL_CARD_MAX; i++) {
                prizeCardDao.insertAnnualPrizeCard(profileId, CommonUtils.randomString(8));
            }
        } else if (percent < 0.5) {
            for (int i = 0; i < ANNUAL_CARD_MIN; i++) {
                prizeCardDao.insertAnnualPrizeCard(profileId, CommonUtils.randomString(8));
            }
        } else {
            for (int i = 0; i < ANNUAL_CARD_MIDDLE; i++) {
                prizeCardDao.insertAnnualPrizeCard(profileId, CommonUtils.randomString(8));
            }
        }
        prizeCards = prizeCardDao.getAnnualPrizeCards(profileId);

        return prizeCards;
    }

    @Override
    public String isPreviewCardReceived(String cardId, Integer profileId) {
        if (!accountService.isPreviewNewUser(profileId)) {
            return "亲,请给新用户一点机会吧~";
        }
        //判断礼品卡是否已经被领取
        PrizeCard prizeCard = prizeCardDao.loadCardByCardNo(cardId);
        if (prizeCard == null) {
            return "该礼品卡不存在";
        }
        if (prizeCard.getUsed()) {
            return "该礼品卡已经被领取";
        }
        //领取礼品卡
        if (prizeCardDao.updatePreviewCard(prizeCard.getId(), profileId) == 0) {
            return "该礼品卡已经被领取";
        }
        //暂时不开课
        generatePlanService.createTeamLearningPlan(profileId);
        return "恭喜您获得该礼品卡";
    }

    @Override
    public void sendReceiveCardMsgSuccessful(String openid, String nickname) {
        String templateMsg = "你好{nickname}，欢迎来到圈外商学院！\n\n" +
                "你已成功领取商学院体验卡！\n\n扫码加小Y，回复\"体验\"，让他带你开启7天线上学习之旅吧！";

        customerMessageService.sendCustomerMessage(openid, templateMsg.replace("{nickname}", nickname), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        customerMessageService.sendCustomerMessage(openid, ConfigUtils.getXiaoYQRCode(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }

    /**
     * 发送领取成功模板消息
     * @param openid
     * @param nickName
     */
    @Override
    public void sendReceivedAnnualMsgSuccessful(String openid, String nickName) {
        String templateMsg = "你好{nickname}，欢迎来到圈外商学院！\n\n" +
                "你已成功领取商学院体验卡！\n\n扫码加小Y，回复\"体验\"，让他带你开启7天线上学习之旅吧！";

       if(!customerMessageService.sendCustomerMessage(openid, templateMsg.replace("{nickname}", nickName), Constants.WEIXIN_MESSAGE_TYPE.TEXT)){
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.getTrialNotice());
            templateMessage.setTouser(openid);
            Map<String,TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            data.put("keyword1",new TemplateMessage.Keyword("圈外商学院体验邀请函"));
            data.put("keyword2",new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("remark",new TemplateMessage.Keyword("\n你已成功领取商学院体验卡！\n" +
                    "\n点击这里，扫码加小Y，回复\"体验\"，让他带你开启7天线上学习之旅吧！"));
            templateMessage.setUrl("https://static.iqycamp.com/images/qrcode/XIAOY_2018_01_02.jpg");
            templateMessageService.sendMessage(templateMessage);
        }else {
            customerMessageService.sendCustomerMessage(openid, ConfigUtils.getXiaoYQRCode(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        }
    }

    @Override
    public void sendReceivedAnnualFailureMsg(String openid, String result) {
        customerMessageService.sendCustomerMessage(openid,result,Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}