package com.iquanwai.platon.biz.domain.common.customer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.AnnualSummaryDao;
import com.iquanwai.platon.biz.dao.fragmentation.GroupPromotionDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardConfigDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.PrizeCardConfig;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrizeCardServiceImpl implements PrizeCardService {
    @Autowired
    private PrizeCardDao prizeCardDao;
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
    private PrizeCardConfigDao prizeCardConfigDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static Integer ANNUAL_CARD_MAX = 5;
    private final static Integer ANNUAL_CARD_MIDDLE = 4;
    private final static Integer ANNUAL_CARD_MIN = 3;


    @Override
    public List<PrizeCard> loadNoOwnerPrizeCard(Integer profileId) {
        List<PrizeCard> prizeCards = prizeCardDao.loadUnreceivedPrizeCard(profileId);

        prizeCards.forEach(prizeCard -> {
            Integer category = prizeCard.getCategory();
            PrizeCardConfig prizeCardConfig = prizeCardConfigDao.loadPrizeCardConfig(category);
            if (prizeCardConfig != null) {
                //判断礼品卡是否被领取
                if (prizeCard.getReceiverProfileId() == null) {
                    prizeCard.setBackground(prizeCardConfig.getCoverPic());
                    prizeCard.setUrl(prizeCardConfig.getDetailUrl());
                } else {
                    prizeCard.setBackground(prizeCardConfig.getReceivedCoverPic());
                }
                //判断礼品卡是否过期
                if (prizeCard.getExpiredTime().before(new Date())) {
                    prizeCard.setExpired(true);
                    prizeCard.setBackground(prizeCardConfig.getExpiredCoverPic());
                } else {
                    prizeCard.setExpired(false);
                }
                prizeCard.setExpiredDate(DateUtils.parseDateToFormat5(prizeCard.getExpiredTime()));
            }
        });

        //已过期<已领取<未领取
        prizeCards.sort((o1, o2) -> {
            int count1 = 0;
            int count2 = 0;
            if(o1.isExpired()){
                count1 = count1 + 10;
            }
            if(o2.isExpired()){
                count2 = count2 + 10;
            }
            if(o1.getReceiverProfileId() != null){
                count1 = count1 + 1;
            }
            if(o2.getReceiverProfileId() != null){
                count2 = count2 + 1;
            }

            return count1 - count2;
        });

        return prizeCards;
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

    @Override
    public boolean ownerCheck(String cardNo, String openid) {
        PrizeCard prizeCard = prizeCardDao.loadCardByCardNo(cardNo);

        if (prizeCard == null) {
            return false;
        }
        Integer profileId = prizeCard.getProfileId();

        Profile profile = accountService.getProfile(profileId);
        return profile != null && profile.getOpenid().equals(openid);
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
    public Pair<Integer, String> isPreviewCardReceived(String cardId, Integer profileId) {
        if (!accountService.isPreviewNewUser(profileId)) {
            return new ImmutablePair<>(-1, "亲，请给新用户一点机会吧~");
        }
        //判断礼品卡是否已经被领取
        PrizeCard prizeCard = prizeCardDao.loadCardByCardNo(cardId);
        if (prizeCard == null) {
            return new ImmutablePair<>(-1, "该礼品卡不存在");
        }
        if (prizeCard.getUsed()) {
            return new ImmutablePair<>(-1, "该礼品卡已经被领取");
        }
        if (prizeCard.getExpiredTime().before(new Date())) {
            return new ImmutablePair<>(-2, "抱歉，商学院体验卡已过期。");
        }
        //领取礼品卡
        if (prizeCardDao.updatePreviewCard(prizeCard.getId(), profileId) == 0) {
            return new ImmutablePair<>(-1, "该礼品卡已经被领取");
        }

        if (prizeCard.getProfileId().equals(profileId)) {
            return new ImmutablePair<>(1, "本人的礼品卡");
        }
        //暂时不开课
        generatePlanService.createTeamLearningPlan(profileId);
        return new ImmutablePair<>(0, "恭喜您获得该礼品卡");
    }

    /**
     * 发送领取成功模板消息
     *
     * @param openid
     * @param nickname
     */
    @Override
    public void sendReceivedAnnualMsgSuccessful(String openid, String nickname) {
        String templateMsg = "你好{nickname}，欢迎来到圈外商学院！\n\n" +
                "你已成功领取商学院体验卡！\n\n扫码加小Y，回复\"体验\"，让他带你开启7天线上学习之旅吧！";

        if (!customerMessageService.sendCustomerMessage(openid, templateMsg.replace("{nickname}", nickname), Constants.WEIXIN_MESSAGE_TYPE.TEXT)) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.getTrialNotice());
            templateMessage.setTouser(openid);
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            data.put("keyword1", new TemplateMessage.Keyword("圈外商学院体验邀请函"));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("remark", new TemplateMessage.Keyword("\n你已成功领取商学院体验卡！\n" +
                    "\n点击这里，扫码加小Y，回复\"体验\"，让他带你开启7天线上学习之旅吧！"));
            templateMessage.setUrl("https://static.iqycamp.com/images/qrcode/XIAOY_2018_01_02.jpg");
            templateMessageService.sendMessage(templateMessage);
        } else {
            customerMessageService.sendCustomerMessage(openid, ConfigUtils.getXiaoYQRCode(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        }
    }

    @Override
    public void sendReceivedAnnualFailureMsg(String openid, String result) {
        customerMessageService.sendCustomerMessage(openid, result, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    @Override
    public void sendAnnualOwnerMsg(String cardNum, String receiver) {
        PrizeCard prizeCard = prizeCardDao.loadCardByCardNo(cardNum);
        if (prizeCard == null) {
            logger.info("礼品卡为空");
            return;
        }
        Profile profile = accountService.getProfile(prizeCard.getProfileId());
        if (profile == null) {
            logger.info("人员为空");
            return;
        }
        logger.info("开始发送模板消息");
        //发送模板消息
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        templateMessage.setTouser(profile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        data.put("keyword1", new TemplateMessage.Keyword("【圈外商学院年度报告】邀请函分享"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("圈外同学公众号"));
        data.put("first", new TemplateMessage.Keyword(receiver + "领取了你的商学院邀请函，开启了7天线上体验之旅！\n"));
        templateMessageService.sendMessage(templateMessage);
    }
}