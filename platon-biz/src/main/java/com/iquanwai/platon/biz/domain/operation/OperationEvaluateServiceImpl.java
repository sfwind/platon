package com.iquanwai.platon.biz.domain.operation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class OperationEvaluateServiceImpl implements OperationEvaluateService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    private static final int SOURCE_RISE = 1; // 官方
    private static final int SOURCE_SELF = 2; // 自己
    private static final int SOURCE_OTHER = 3; // 他人

    private static final Integer TRIAL_NUM = 3;
    private static final String ACTIVITY = PromotionConstants.Activities.EVALUATE;

    private static Map<Integer, String> evaluateResultMap = Maps.newHashMap(); // 测评结果
    private static Map<Integer, String> suggestionMap = Maps.newHashMap(); // 测评建议

    private final static String TEMP_IMAGE_PATH = "/data/static/images/";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {

        evaluateResultMap.put(1, "你的这方面潜力还没有得到开发和施展。你努力地工作生活，但常常感到受压抑，因为面对问题时，你可能忽视了本质原因，从而不能很有效地解决问题。");
        evaluateResultMap.put(2, "拥有洞察力潜力的你，比较关注细节，能够准确地把握事实；但有时候，因为没有把潜力发挥出来，而做了一些无用功，觉得自己的付出得不到应有的回报。");
        evaluateResultMap.put(3, "你具备很高的洞察力天赋！很快就能洞悉问题本质，人际交往中，往往也能准确领会他人的意图。");

        suggestionMap.put(1, "你需要开始发力去增强自己的洞察力并应用在职场中，在面对棘手问题时:\n\n" +
                "  1. 有意识地使用提问的技巧\n" +
                "  2. 通过了解背景，挖掘他人需求\n" +
                "  3. 防止盲目的决策和行动\n\n" +
                "当你的能够正确领会他人意图，并找到问题的本质原因，就能顺利解决问题，得到同事和老板的认可啦。");

        suggestionMap.put(2, "根据测评结果，我们建议你:\n\n" +
                "  1. 掌握更多的提问技巧，挖掘他人隐藏的真实需求\n" +
                "  2. 提升自己的分析能力，遇到难题时，先找到根本原因\n" +
                "  3. 找到根本原因后，根据关键程度和解决成本，定位最有价值的问题\n\n" +
                "当你的大量时间都在解决高价值问题时，就能成为在职场上游刃有余的高效能人士啦。");

        suggestionMap.put(3, "你需要在工作中充分运用你的洞察力天赋。在找到本质问题后，先不要急于解决，而是分析关键程度和解决成本，再采取对应的行动。\n\n" +
                "当你的大量时间都在解决高价值问题时，就能成为传说中 “不加班也能升职、看透他人心思人缘爆表、提议文案一次通过”的异能人士啦。");


        // 创建图片保存目录
        File file = new File(TEMP_IMAGE_PATH);
        if (!file.exists()) {
            if (!file.mkdir()) {
                logger.error("创建活动图片临时保存目录失败!!!");
            }
        }
    }

    /**
     * 点击链接
     */
    @Override
    public void clickHref(Integer profileId) {
        recordPromotionLevel(profileId, "RISE");
        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.CLICK_HREF);
    }

    /**
     * 触发扫码时间，对应记录修改
     */
    @Override
    public void recordScan(Integer profileId, String source) {
        recordPromotionLevel(profileId, source);
        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.SCAN_CARD);
    }

    /**
     * 完成测评
     */
    @Override
    public Pair<String, String> completeEvaluate(Integer profileId, Integer score, Boolean freeLimit, Integer percent) {
        // 如果不是 level 中的人则不记录
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, ACTIVITY);
        if (promotionLevel != null) {
            recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.FINISH_EVALUATE);
            List<Integer> actions = Lists.newArrayList();
            actions.add(PromotionConstants.EvaluateAction.FINISH_EVALUATE);
            actions.add(PromotionConstants.EvaluateAction.BUY_COURSE);
            List<PromotionActivity> activities = promotionActivityDao.loadActionList(profileId,
                    actions, ACTIVITY);
            if (activities.size() == 1) {
                checkAwardAndSendMsg(profileId);
            }
        }

        Integer level = calcLevel(score);

        return new ImmutablePair<>(evaluateResultMap.get(level),
                suggestionMap.get(level));
    }

    /**
     * 记录付费购买操作
     */
    @Override
    public void recordPayAction(Integer profileId) {
        // 如果不是 level 中的人则不记录
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, ACTIVITY);
        if (promotionLevel == null) {
            return;
        }

        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.BUY_COURSE);

        List<Integer> actions = Lists.newArrayList();
        actions.add(PromotionConstants.EvaluateAction.FINISH_EVALUATE);
        actions.add(PromotionConstants.EvaluateAction.BUY_COURSE);
        List<PromotionActivity> activities = promotionActivityDao.loadActionList(profileId,
                actions, ACTIVITY);
        if (activities.size() == 1) {
            checkAwardAndSendMsg(profileId);
        }
    }

    /**
     * 免费试用限免课权限校验
     */
    @Override
    public boolean checkTrialAuthority(Integer profileId) {
        List<PromotionActivity> activities = promotionActivityDao.loadPromotionActivities(profileId, ACTIVITY);
        Long accessTrialCnt = activities.stream().filter(
                activity -> activity.getAction() == PromotionConstants.EvaluateAction.ACCESS_TRIAL
        ).count();
        return accessTrialCnt.intValue() > 0;
    }


    // 根据得分计算得分 level
    private Integer calcLevel(Integer score) {
        if (score >= 7) {
            return 3;
        } else if (score >= 3) {
            return 2;
        } else {
            return 1;
        }
    }

    // 获取二维码，场景值变化
    private BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = qrCodeService.generateTemporaryQRCode(scene, null);
        InputStream inputStream = qrCodeService.showQRCode(response.getTicket());
        try {
            return ImageUtils.getBufferedImageByInputStream(inputStream);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("is close failed", e);
            }
        }
    }

    // 获取用户头像
    private BufferedImage loadHeadImage(Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        // 获取用户头像图片
        String headImgUrl = profile.getHeadimgurl();
        BufferedImage headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        // 如果用户头像过期，则拉取实时新头像
        if (headImg == null) {
            Account realProfile;
            try {
                realProfile = accountService.getAccount(profile.getOpenid(), true);
                headImgUrl = realProfile.getHeadimgurl();
                headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
            } catch (NotFollowingException e) {
                // ignore
            }
        }
        // 修复两次都没有头像的用户，使用默认头像
        if (headImg == null) {
            String defaultImageUrl = "https://static.iqycamp.com/images/fragment/headimg_default_1.jpg?imageslim";
            headImg = ImageUtils.getBufferedImageByUrl(defaultImageUrl);
        }
        return headImg;
    }

    // 查看获取当前已经获取的奖励，并且同时发送消息
    private void checkAwardAndSendMsg(Integer profileId) {
        // 某人完成测评，需要查看他的推广人信息
        PromotionLevel source = promotionLevelDao.loadByProfileId(profileId, ACTIVITY);
        if (source == null || source.getPromoterId() == null) {
            return;
        }

        // 推广人id
        Integer sourceProfileId = source.getPromoterId();

        boolean freeLimit = false;
        ImprovementPlan improvementPlan = improvementPlanDao.
                loadPlanByProblemId(sourceProfileId, ConfigUtils.getTrialProblemId());
        if(improvementPlan!=null){
            freeLimit = true;
        } else {
            Profile sourceProfile = accountService.getProfile(sourceProfileId);
            if(sourceProfile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP){
                freeLimit = true;
            }
        }

        Integer remainTrial = accessTrial(sourceProfileId);
        // 发送给推广人的消息
        if (remainTrial == 0) {
            if(!freeLimit){
                sendSuccessTrialMsg(profileId, sourceProfileId);
            } else {
                sendNormalTrialMsg(profileId, sourceProfileId, remainTrial, true);
            }
        } else if (remainTrial > 0) {
            sendNormalTrialMsg(profileId, sourceProfileId, remainTrial, freeLimit);
        }
    }

    // 常看当前此人是否有权限获得获得限免试用资格
    private Integer accessTrial(Integer profileId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(profileId, ACTIVITY);
        List<Integer> profileIds = promotionLevels.stream().map(PromotionLevel::getProfileId).collect(Collectors.toList());
        List<PromotionActivity> newUsers = promotionActivityDao.loadNewUsers(profileIds, ACTIVITY);

        List<PromotionActivity> successUsers = newUsers.stream().filter(
                user -> user.getAction() == PromotionConstants.EvaluateAction.FINISH_EVALUATE
                        || user.getAction() == PromotionConstants.EvaluateAction.BUY_COURSE
        ).filter(distinctByKey(PromotionActivity::getProfileId)).collect(Collectors.toList());

        Integer remainTrial = -1;

        // 达到试用人数要求，获得试用权限
        if (successUsers.size() == TRIAL_NUM) {
            int result = recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.ACCESS_TRIAL);
            if (result > 0) {
                remainTrial = 0;
            }
        } else if (successUsers.size() > TRIAL_NUM) {
            remainTrial = -1;
        } else {
            remainTrial = TRIAL_NUM - successUsers.size();
        }

        return remainTrial;
    }

    // 发送普通限免小课信息
    private void sendNormalTrialMsg(Integer promotedUserId, Integer targetProfileId, Integer remainCount, Boolean freeLimit) {
        Profile targetProfile = accountService.getProfile(targetProfileId);
        Profile promoterProfile = accountService.getProfile(promotedUserId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetProfile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        templateMessage.setData(data);

        if(!freeLimit){
            if (remainCount != 0) {
                data.put("first", new TemplateMessage.Keyword("你的好友" + promoterProfile.getNickname() + "扫码完成测试，距离免费领取洞察力小课，只剩"
                        + remainCount + "个好友啦！\n"));
            } else {
                data.put("first", new TemplateMessage.Keyword("你已获得免费领取洞察力小课资格啦！\n"));
            }
            data.put("keyword1", new TemplateMessage.Keyword("洞察力天赋检测"));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        }else{
            data.put("first", new TemplateMessage.Keyword("你的好友"+ promoterProfile.getNickname() + "扫码完成测试啦，ta一定很关注你！\n"));
            data.put("keyword1", new TemplateMessage.Keyword("洞察力天赋检测"));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
            data.put("remark", new TemplateMessage.Keyword("\n为了不打扰到你，超过3位好友扫码就不再提醒啦~"));
        }

        templateMessageService.sendMessage(templateMessage);
    }

    // 发送成功获得限免小课试用信息
    private void sendSuccessTrialMsg(Integer promotedUserId, Integer targetProfileId) {
        // 发送模板消息
        // 被推荐人
        Profile promoterProfile = accountService.getProfile(promotedUserId);
        // 推荐人
        Profile profile = accountService.getProfile(targetProfileId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(profile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(ConfigUtils.domainName() + "/rise/static/plan/view?id=9&free=true");
        templateMessage.setTemplate_id(ConfigUtils.getSignUpSuccessMsg());
        data.put("first", new TemplateMessage.Keyword("恭喜！你的好友"+promoterProfile.getNickname() + "扫码完成测试，总计"+
                TRIAL_NUM + "位好友完成测评，你已获得免费领取洞察力小课资格啦！\n"));
        data.put("keyword1", new TemplateMessage.Keyword("找到本质问题，减少无效努力"));
        data.put("keyword2", new TemplateMessage.Keyword("圈外同学"));
        data.put("remark", new TemplateMessage.Keyword("\n点击卡片领取！"));
        templateMessageService.sendMessage(templateMessage);


        // 非会员，限免小课 TODO  删除这边时候，记得开放上面 TODO
        // 发送我的消息，发送图片
        if (!profile.getRiseMember().equals(Constants.RISE_MEMBER.MEMBERSHIP)) {
            TemplateMessage tempMessage = new TemplateMessage();
            tempMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> tempDate = Maps.newHashMap();
            tempMessage.setData(tempDate);
            tempMessage.setUrl("https://shimo.im/doc/892xxjNdSRA3UTSG?r=L8QE82/");
            tempMessage.setTemplate_id(ConfigUtils.getSignUpSuccessMsg());
            tempDate.put("first", new TemplateMessage.Keyword("这么赞的训练营，我们真的只能免费这一次！【圈外同学】开放迷你训练营，你要不要来？\n"));
            tempDate.put("keyword1", new TemplateMessage.Keyword("【圈外同学】迷你训练营"));
            tempDate.put("keyword2", new TemplateMessage.Keyword("点击“详情”，按照步骤操作入群"));
            tempDate.put("keyword3", new TemplateMessage.Keyword("2017年9月19日-2017年9月20日"));
            tempDate.put("remark", new TemplateMessage.Keyword("\n迷你训练营为【圈外同学】¥299的“小课训练营”的精华版，本次免费开放，等你来加入～\n" +
                    "名额限500人，先到先得～"));
            templateMessageService.sendMessage(tempMessage);


            String innerMessage = "【圈外同学】迷你训练营邀你免费入群体验——为期4日魔鬼训练营，敢来的戳这里入群～";
            messageService.sendMessage(innerMessage, String.valueOf(profile.getId()), MessageService.SYSTEM_MESSAGE, "https://shimo.im/doc/892xxjNdSRA3UTSG?r=L8QE82/");
        }
    }

    // 记录 PromotionLevel 层级关系
    private void recordPromotionLevel(Integer profileId, String source) {
        if (isExistInPromotionLevel(profileId)) {
            // 当前扫码人已经在 promotionLevel 表，则直接跳过
            return;
        }

        if (isRiseMember(profileId)) {
            // 如果此人是会员
            PromotionLevel level = getDefaultPromotionLevel();
            level.setProfileId(profileId);
            level.setValid(0);
            level.setLevel(1);
            promotionLevelDao.insertPromotionLevel(level);
            return;
        }

        PromotionLevel selfLevel = getDefaultPromotionLevel();
        selfLevel.setProfileId(profileId);
        Integer scanSource = scanSource(profileId, source);
        switch (scanSource) {
            case SOURCE_RISE:
                selfLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(selfLevel);
                break;
            case SOURCE_SELF:
                selfLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(selfLevel);
                break;
            case SOURCE_OTHER:
                // 扫他人码
                Integer sourceId = Integer.parseInt(source);
                if (isExistInPromotionLevel(sourceId)) {
                    // 他人已经存在于 level 表
                    PromotionLevel otherLevel = promotionLevelDao.loadByProfileId(sourceId, ACTIVITY);
                    Integer baseLevel = otherLevel.getLevel();
                    selfLevel.setLevel(baseLevel + 1);
                    selfLevel.setPromoterId(sourceId);
                    promotionLevelDao.insertPromotionLevel(selfLevel);
                } else {
                    // 他人不存在于 level 表
                    PromotionLevel otherLevel = getDefaultPromotionLevel();
                    otherLevel.setProfileId(sourceId);
                    otherLevel.setLevel(1);
                    otherLevel.setValid(0);
                    promotionLevelDao.insertPromotionLevel(otherLevel);

                    selfLevel.setPromoterId(sourceId);
                    selfLevel.setLevel(2);
                    promotionLevelDao.insertPromotionLevel(selfLevel);
                }
                break;
            default:
                // pass
        }
    }

    // 记录 PromotionActivity 行为活动
    private Integer recordPromotionActivity(Integer profileId, Integer action) {
        PromotionActivity promotionActivity = getDefaultPromotionActivity();
        promotionActivity.setProfileId(profileId);
        promotionActivity.setAction(action);
        return promotionActivityDao.insertPromotionActivity(promotionActivity);
    }

    // 在 level 表中是否存在记录
    private boolean isExistInPromotionLevel(Integer profileId) {
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, ACTIVITY);
        return promotionLevel != null;
    }

    // 扫码者是否是会员
    private boolean isRiseMember(Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        return profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP;
    }

    // 扫码来源，区分为 1-官方， 2-自己， 3-他人
    private Integer scanSource(Integer selfProfileId, String source) {
        if ("RISE".equalsIgnoreCase(source)) {
            return SOURCE_RISE;
        } else if (selfProfileId.toString().equalsIgnoreCase(source)) {
            return SOURCE_SELF;
        } else {
            return SOURCE_OTHER;
        }
    }

    private PromotionLevel getDefaultPromotionLevel() {
        PromotionLevel promotionLevel = new PromotionLevel();
        promotionLevel.setActivity(ACTIVITY);
        promotionLevel.setValid(1);
        return promotionLevel;
    }

    private PromotionActivity getDefaultPromotionActivity() {
        PromotionActivity promotionActivity = new PromotionActivity();
        promotionActivity.setActivity(ACTIVITY);
        return promotionActivity;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = Maps.newConcurrentMap();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
