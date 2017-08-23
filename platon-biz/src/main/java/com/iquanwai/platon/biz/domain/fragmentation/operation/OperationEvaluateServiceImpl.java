package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
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
    private UploadResourceService uploadResourceService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;

    private static final int Source_RISE = 1; // 官方
    private static final int Source_SELF = 2; // 自己
    private static final int Source_Other = 3; // 他人

    private static final Integer trialNum = 3;
    private static final String activity = PromotionConstants.Activities.Evaluate;

    private static Map<Integer, BufferedImage> targetImageMap = Maps.newHashMap(); // 预先加载好所有背景图
    private static Map<Integer, String> evaluateResultMap = Maps.newHashMap(); // 预先加载好所有背景图
    private static Map<Integer, String> suggestionMap = Maps.newHashMap(); // 预先加载好所有背景图
    private static Map<Integer, String> resultTextMap = Maps.newHashMap(); // 发送测评结果

    private final static String TEMP_IMAGE_PATH = "/data/static/images/";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        targetImageMap.put(1, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate1_5.png?imageslim"));
        targetImageMap.put(2, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate2_5.png?imageslim"));
        targetImageMap.put(3, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate3_5.png?imageslim"));

        evaluateResultMap.put(1, "你的职场洞察力还没有得到开发和施展。你努力地工作生活，但常常感到受压抑，因为面对问题时，你更多地采用个人的主观评判，可能忽视了问题的本质原因，从而不能很有效地解决问题。");
        evaluateResultMap.put(2, "拥有敏锐度潜力的你，比较关注细节，能够准确地把握事实；但有时候，因为没有把潜力发挥出来，而做了一些无用功，觉得自己的付出得不到应有的回报，难免有点小失望。");
        evaluateResultMap.put(3, "你的职场敏锐度天赋很高，很快就能洞悉问题本质，人际交往中，往往也能准确领会他人的意图。");

        suggestionMap.put(1, "你需要在工作中充分运用你的敏锐度天赋。在找到本质问题后，先不要急于解决，而是分析关键程度和解决成本，再采取对应的行动。当你的大量时间都在解决高价值问题时，就能成为传说中 “不加班也能升职、看透他人心思人缘爆表、提议文案一次通过”的异能人士啦。\n" +
                "\n" +
                "如果你想要挖掘、并在职场中运用自己的敏锐度天赋，可以使用【职场敏锐度强化包】。据说之前的小伙伴，有人已经跳槽成功，薪资连涨三倍。\n" +
                "\n" +
                "【敏锐度强化包】将教会你四大技巧，内含12条语音、6套巩固练习、10套应用练习，3场吊打直播，1套知识卡片，1套牛人干货文章合集。");

        suggestionMap.put(2, "建议你掌握更多的提问技巧，挖掘他人隐藏的真实需求；同时提升自己的分析能力，遇到难题时，先去找到根本原因，再根据关键程度和解决成本，定位最有价值的问题。当你的大量时间都在解决高价值问题时，就能成为在职场上游刃有余的高效能人士啦。\n" +
                "\n" +
                "如果你想要充分发挥自己的敏锐度潜力，可以使用【职场敏锐度强化包】。据说之前的小伙伴，有人已经跳槽成功，薪资连涨三倍。\n" +
                "\n" +
                "【敏锐度强化包】将教会你四大技巧，内含12条语音、6套巩固练习、10套应用练习，3场吊打直播，1套知识卡片，1套牛人干货文章合集。");

        suggestionMap.put(3, "你需要开始发力去增强自己的敏锐度并应用在职场中，在面对棘手问题时，有意识地使用一些技巧，例如提问的技巧、了解他人背景挖掘他人需求的技巧等，防止盲目的决策和行动。当你的能够正确领会他人意图，并找到问题的本质原因，就能顺利解决问题，得到同事和老板的认可啦。\n" +
                "\n" +
                "如果你想要充分开发和增强自己的敏锐度，可以使用【职场敏锐度强化包】。据说之前的小伙伴，有人已经跳槽成功，薪资连涨三倍。\n" +
                "\n" +
                "【敏锐度强化包】将教会你四大技巧，内含12条语音、6套巩固练习、10套应用练习，3场吊打直播，1套知识卡片，1套牛人干货文章合集。");

        resultTextMap.put(1, "这么有趣有料的测试，确定不邀请你的朋友也来玩一玩吗？快去保存并分享到朋友圈吧！");
        resultTextMap.put(2, "敢不敢分享到朋友圈，让你的朋友也挑战一下！");
        resultTextMap.put(3, "下方是你的测评结果海报。你的朋友们也和你一样机智吗？分享出来，让他们也检测一下吧！");

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
        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.ClickHref);
    }

    /**
     * 触发扫码时间，对应记录修改
     */
    @Override
    public void recordScan(Integer profileId, String source) {
        recordPromotionLevel(profileId, source);
        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.ScanCard);
    }

    /**
     * 完成测评
     */
    @Override
    public Pair<String, String> completeEvaluate(Integer profileId, Integer score) {
        // 如果不是 level 中的人则不记录
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, activity);
        if (promotionLevel != null) {
            recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.FinishEvaluate);

            List<PromotionActivity> activities = promotionActivityDao.loadDistinctActionCount(profileId,
                    PromotionConstants.EvaluateAction.FinishEvaluate, activity);
            if (activities.size() == 1) {
                checkAwardAndSendMsg(profileId);
            }
        }

        Integer level = calcLevel(score);

        return new ImmutablePair<>(evaluateResultMap.get(level), suggestionMap.get(level));
    }

    /**
     * 记录付费购买操作
     */
    @Override
    public void recordPayAction(Integer profileId) {
        // 如果不是 level 中的人则不记录
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, activity);
        if (promotionLevel == null) return;

        recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.BuyCourse);

        List<PromotionActivity> activities = promotionActivityDao.loadDistinctActionCount(profileId,
                PromotionConstants.EvaluateAction.FinishEvaluate, activity);
        if (activities.size() == 1) {
            checkAwardAndSendMsg(profileId);
        }
    }

    /**
     * 免费试用限免课权限校验
     */
    @Override
    public boolean checkTrialAuthority(Integer profileId) {
        List<PromotionActivity> activities = promotionActivityDao.loadPromotionActivities(profileId, activity);
        Long accessTrialCnt = activities.stream().filter(
                activity -> activity.getAction() == PromotionConstants.EvaluateAction.AccessTrial
        ).count();
        return accessTrialCnt.intValue() > 0;
    }

    /**
     * 微信后台推送结果卡片
     */
    @Override
    public void sendPromotionResult(Integer profileId, Integer score, Boolean learnFreeLimit) {
        Profile profile = accountService.getProfile(profileId);
        // 计算测评等级
        Integer level = calcLevel(score);

        Assert.notNull(profile, "用户不能为空");
        customerMessageService.sendCustomerMessage(
                profile.getOpenid(),
                resultTextMap.get(level),
                Constants.WEIXIN_MESSAGE_TYPE.TEXT
        );

        Integer percent = getDefeatPercent(score);
        BufferedImage bufferedImage = generateResultPic(profileId, level, percent);
        Assert.notNull(bufferedImage, "生成图片不能为空");

        // 发送图片消息
        String path = TEMP_IMAGE_PATH + CommonUtils.randomString(10) + profileId + ".jpg";
        String mediaId = uploadResourceService.uploadResource(bufferedImage, path);
        customerMessageService.sendCustomerMessage(profile.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }

    /**
     * 是否参加过此活动
     */
    @Override
    public boolean hasParticipateEvaluate(Integer profileId) {
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, activity);
        return promotionLevel != null;
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

    // 生成结果图片
    private BufferedImage generateResultPic(Integer profileId, Integer level, Integer percent) {
        BufferedImage targetImage = targetImageMap.get(level);
        BufferedImage qrImage = loadQrImage(PromotionConstants.Activities.Evaluate + "_" + profileId + "_9");
        BufferedImage headImage = loadHeadImage(profileId);

        InputStream in = getClass().getResourceAsStream("/fonts/pfmedium.ttf");
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }

        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
        qrImage = ImageUtils.scaleByPercentage(qrImage, 214, 214);
        headImage = ImageUtils.scaleByPercentage(headImage, 120, 120);
        headImage = ImageUtils.convertCircular(headImage);

        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 101, 1025);
        targetImage = ImageUtils.overlapImage(targetImage, headImage, 129, 273);

        Profile profile = accountService.getProfile(profileId);

        targetImage = ImageUtils.writeText(targetImage, 280, 320,profile.getNickname()+"的职场敏锐度",
                font.deriveFont(30f), new Color(255, 255, 255));
        targetImage = ImageUtils.writeText(targetImage, 280, 365, "打败了"+percent+"%的人",
                font.deriveFont(30f), new Color(255, 255, 255));
        return targetImage;
    }

    // 获取二维码，场景值变化
    private BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = qrCodeService.generateTemporaryQRCode(scene, null);
        InputStream inputStream = qrCodeService.showQRCode(response.getTicket());
        return ImageUtils.getBufferedImageByInputStream(inputStream);
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
        PromotionLevel source = promotionLevelDao.loadByProfileId(profileId, activity);
        if (source == null || source.getPromoterId() == null) return;

        Integer sourceId = source.getPromoterId();

        Integer remainTrial = accessTrial(sourceId);
        if (remainTrial == 0) {
            sendNormalTrialMsg(sourceId, profileId, remainTrial);
            sendSuccessTrialMsg(sourceId);
        } else if (remainTrial > 0) {
            sendNormalTrialMsg(sourceId, profileId, remainTrial);
        }
    }

    // 常看当前此人是否有权限获得获得限免试用资格
    private Integer accessTrial(Integer profileId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(profileId, activity);
        List<Integer> profileIds = promotionLevels.stream().map(PromotionLevel::getProfileId).collect(Collectors.toList());
        List<PromotionActivity> newUsers = promotionActivityDao.loadNewUsers(profileIds, activity);

        List<PromotionActivity> successUsers = newUsers.stream().filter(
                user -> user.getAction() == PromotionConstants.EvaluateAction.FinishEvaluate
                        || user.getAction() == PromotionConstants.EvaluateAction.BuyCourse
        ).filter(distinctByKey(PromotionActivity::getProfileId)).collect(Collectors.toList());

        Integer remainTrial = -1;

        // 达到试用人数要求，获得试用权限
        if (successUsers.size() == trialNum) {
            int result = recordPromotionActivity(profileId, PromotionConstants.EvaluateAction.AccessTrial);
            if (result > 0) {
                remainTrial = 0;
            }
        } else if (successUsers.size() > trialNum) {
            remainTrial = -1;
        } else {
            remainTrial = trialNum - successUsers.size();
        }

        return remainTrial;
    }

    // 发送普通限免小课信息
    private void sendNormalTrialMsg(Integer targetProfileId, Integer promotedUserId, Integer remainCount) {
        Profile targetProfile = accountService.getProfile(targetProfileId);
        Profile promoterProfile = accountService.getProfile(promotedUserId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetProfile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        templateMessage.setData(data);

        if (remainCount == 0) {
            data.put("first", new TemplateMessage.Keyword("你的好友" + promoterProfile.getNickname() + "扫码完成测试，距离免费领取洞察力小课，只剩"
                    + remainCount + "个好友啦！\n"));
        } else {
            data.put("first", new TemplateMessage.Keyword("你已获得免费领取洞察力小课资格啦！\n"));
        }
        data.put("keyword1", new TemplateMessage.Keyword("洞察力基因检测"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        templateMessageService.sendMessage(templateMessage);
    }

    // 发送成功获得限免小课试用信息
    private void sendSuccessTrialMsg(Integer profileId) {
        // 发送模板消息
        Profile profile = accountService.getProfile(profileId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(profile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(ConfigUtils.domainName() + "/rise/static/plan/view?id=9&free=true");
        templateMessage.setTemplate_id(ConfigUtils.getSignUpSuccessMsg());
        data.put("first", new TemplateMessage.Keyword("恭喜！" + trialNum + "位好友完成测评，你已获得免费领取洞察力小课资格啦！\n"));
        data.put("keyword1", new TemplateMessage.Keyword("找到本质问题，减少无效努力"));
        data.put("keyword2", new TemplateMessage.Keyword("圈外同学"));
        data.put("remark", new TemplateMessage.Keyword("\n点击卡片领取！"));
        templateMessageService.sendMessage(templateMessage);

        // 发送客服消息
        customerMessageService.sendCustomerMessage(profile.getOpenid(),
                "点击免费领取洞察力小课：\n" +
                        "<a href='" + ConfigUtils.domainName() + "/rise/static/plan/view?id=9&free=true'>找到本质问题，减少无效努力</a>",
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
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
            case Source_RISE:
                selfLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(selfLevel);
                break;
            case Source_SELF:
                selfLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(selfLevel);
                break;
            case Source_Other:
                // 扫他人码
                Integer sourceId = Integer.parseInt(source);
                if (isExistInPromotionLevel(sourceId)) {
                    // 他人已经存在于 level 表
                    PromotionLevel otherLevel = promotionLevelDao.loadByProfileId(sourceId, activity);
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
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profileId, activity);
        return promotionLevel != null;
    }

    // 扫码者是否是会员
    private boolean isRiseMember(Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        return profile.getRiseMember() == 1;
    }

    // 扫码来源，区分为 1-官方， 2-自己， 3-他人
    private Integer scanSource(Integer selfProfileId, String source) {
        if ("RISE".equalsIgnoreCase(source)) {
            return Source_RISE;
        } else if (selfProfileId.toString().equalsIgnoreCase(source)) {
            return Source_SELF;
        } else {
            return Source_Other;
        }
    }

    private PromotionLevel getDefaultPromotionLevel() {
        PromotionLevel promotionLevel = new PromotionLevel();
        promotionLevel.setActivity(activity);
        promotionLevel.setValid(1);
        return promotionLevel;
    }

    private PromotionActivity getDefaultPromotionActivity() {
        PromotionActivity promotionActivity = new PromotionActivity();
        promotionActivity.setActivity(activity);
        return promotionActivity;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    private static Integer getDefeatPercent(Integer score) {
        switch (score) {
            case 0:
                return new Random().nextInt(20) + 10;
            case 1:
                return new Random().nextInt(20) + 30;
            case 2:
                return new Random().nextInt(10) + 50;
            case 3:
                return new Random().nextInt(10) + 60;
            case 4:
                return new Random().nextInt(10) + 70;
            case 5:
                return new Random().nextInt(10) + 80;
            case 6:
                return new Random().nextInt(5) + 90;
            case 7:
                return new Random().nextInt(5) + 95;
            default:
                return new Random().nextInt(10);
        }
    }

//    public static void main(String[] args) throws Exception{
//        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate1_5.png?imageslim");
////        BufferedImage qrImage = loadQrImage(PromotionConstants.Activities.Evaluate + "_" + profileId + "_9");
//        BufferedImage headImage = ImageUtils.getBufferedImageByUrl("http://wx.qlogo.cn/mmopen/Q3auHgzwzM6LrkJRYApibxYsAEYm2CmS7JZwX09AmHsP0X2VJQSpibHyoHsQKNcvqf1hzFgJr6l40vyhH7KtGWupGmgKHwFibbiaOOS0qKuvjsQ/0");
////
//        InputStream in = OperationEvaluateServiceImpl.class.getResourceAsStream("/fonts/pfmedium.ttf");
//        Font font = Font.createFont(Font.TRUETYPE_FONT, in);
////
////        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
////        qrImage = ImageUtils.scaleByPercentage(qrImage, 214, 214);
//        headImage = ImageUtils.scaleByPercentage(headImage, 120, 120);
//        headImage = ImageUtils.convertCircular(headImage);
////
////        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 101, 1025);
//        targetImage = ImageUtils.overlapImage(targetImage, headImage, 129, 273);
//
//        targetImage = ImageUtils.writeText(targetImage, 280, 320,"风之伤"+"的职场敏锐度",
//                font.deriveFont(30f), new Color(255, 255, 255));
//        targetImage = ImageUtils.writeText(targetImage, 280, 365, "打败了"+80+"%的人",
//                font.deriveFont(30f), new Color(255, 255, 255));
//        ImageIO.write(targetImage, "jpg", new File("/Users/justin/1.jpg"));
//    }
}
