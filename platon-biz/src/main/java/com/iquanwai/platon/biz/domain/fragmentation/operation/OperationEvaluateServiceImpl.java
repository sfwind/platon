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
    // 免费领取测评文案
    private static final String FREE_ACCESS_TEXT = "【免费领取】\n下方是你的测评结果海报，分享并邀请3人扫码并完成测试，即可免费领取【职场敏锐度强化包】";
    // 已学过用户或会员
    private static final String LEARNT_TEXT = "下方是你的测评结果海报。你的朋友们也和你一样机智吗？分享出来，让他们也检测一下吧！" +
            "\n敢不敢分享到朋友圈，让你的朋友也挑战一下" +
            "\n这么有趣有料的测试，确定不邀请你的朋友也来玩一玩吗？快去保存并分享到朋友圈吧！";

    private final static String TEMP_IMAGE_PATH = "/data/static/images/";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        targetImageMap.put(1, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate1_5.png?imageslim"));
        targetImageMap.put(2, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate2_5.png?imageslim"));
        targetImageMap.put(3, ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate3_5.png?imageslim"));

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
    public void completeEvaluate(Integer profileId, Integer score) {
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
        if (learnFreeLimit) {
            // 已学过的用户或会员
            customerMessageService.sendCustomerMessage(
                    profile.getOpenid(),
                    LEARNT_TEXT,
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT
            );
        } else {
            // 未学过的用户
            customerMessageService.sendCustomerMessage(
                    profile.getOpenid(),
                    FREE_ACCESS_TEXT,
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT
            );
        }

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
        if (score >= 8) {
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
        targetImage = ImageUtils.overlapImage(targetImage, headImage, 319, 280);

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
                return new Random().nextInt(10) + 10;
            case 1:
                return new Random().nextInt(10) + 20;
            case 2:
                return new Random().nextInt(10) + 40;
            case 3:
                return new Random().nextInt(10) + 50;
            case 4:
                return new Random().nextInt(10) + 60;
            case 5:
                return new Random().nextInt(10) + 70;
            case 6:
                return new Random().nextInt(10) + 80;
            case 7:
                return new Random().nextInt(5) + 90;
            case 8:
                return new Random().nextInt(5) + 95;
            default:
                return new Random().nextInt(10) + 10;
        }
    }

//    public static void main(String[] args) throws Exception{
//        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/evaluate1_5.png?imageslim");
////        BufferedImage qrImage = loadQrImage(PromotionConstants.Activities.Evaluate + "_" + profileId + "_9");
////        BufferedImage headImage = loadHeadImage(profileId);
////
//        InputStream in = OperationEvaluateServiceImpl.class.getResourceAsStream("/fonts/pfmedium.ttf");
//        Font font = Font.createFont(Font.TRUETYPE_FONT, in);
////
////        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
////        qrImage = ImageUtils.scaleByPercentage(qrImage, 214, 214);
////        headImage = ImageUtils.scaleByPercentage(headImage, 120, 120);
////        headImage = ImageUtils.convertCircular(headImage);
////
////        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 101, 1025);
////        targetImage = ImageUtils.overlapImage(targetImage, headImage, 319, 280);
//
//        targetImage = ImageUtils.writeText(targetImage, 280, 320,profile.getNickname()+"的职场敏锐度",
//                font.deriveFont(30f), new Color(255, 255, 255));
//        targetImage = ImageUtils.writeText(targetImage, 280, 365, "打败了"+percent+"%的人",
//                font.deriveFont(30f), new Color(255, 255, 255));
//        ImageIO.write(targetImage, "jpg", new File("/Users/justin/1.jpg"));
//    }
}
