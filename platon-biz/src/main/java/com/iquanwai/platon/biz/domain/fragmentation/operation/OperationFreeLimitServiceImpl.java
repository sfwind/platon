package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CardRepository;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.*;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class OperationFreeLimitServiceImpl implements OperationFreeLimitService {

    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UploadResourceService uploadResourceService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RabbitMQPublisher rabbitMQPublisher;

    private final static String cacheReloadTopic = "confucius_resource_reload";
    // 推广的熊猫卡临时存放路径
    private final static String TEMP_IMAGE_PATH = "/data/static/images/";
    // 推广成功人数限额
    private final static Integer successNum = ConfigUtils.getFreeLimitSuccessCnt();
    // 推广活动名称
    private final static String activity = PromotionConstants.Activities.FreeLimit;

    @PostConstruct
    public void init() {
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(cacheReloadTopic);
        // 创建图片保存目录
        File file = new File(TEMP_IMAGE_PATH);
        if (!file.exists()) {
            if (!file.mkdir()) {
                logger.error("创建活动图片临时保存目录失败!!!");
            }
        }
    }

    @Override
    public void recordPromotionLevel(String openId, String scene) {
        Profile profile = accountService.getProfile(openId);
        Assert.notNull(profile, "扫码用户不能为空");

        PromotionLevel tempPromotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), activity);
        if (tempPromotionLevel != null) return; // 不是本次活动，或者说已被其他用户推广则不算新人

        String[] sceneParams = scene.split("_");
        if (profile.getRiseMember() == 1) {
            // 如果是会员扫码，则入 level 表，valid = 0，level = 1
            PromotionLevel promotionLevel = getDefaultPromotionLevel();
            promotionLevel.setProfileId(profile.getId());
            promotionLevel.setLevel(1);
            promotionLevel.setValid(0);
            promotionLevelDao.insertPromotionLevel(promotionLevel);
            return;
        }

        if ("RISE".equals(sceneParams[1])) {
            PromotionLevel promotionLevel = getDefaultPromotionLevel();
            promotionLevel.setProfileId(profile.getId());
            promotionLevel.setLevel(1);
            promotionLevelDao.insertPromotionLevel(promotionLevel);
        } else {
            Integer promotionProfileId = Integer.parseInt(sceneParams[1]);
            Profile promotionProfile = accountService.getProfile(promotionProfileId);
            String promotionOpenId = promotionProfile.getOpenid(); // 推广人的 OpenId
            if (openId.equals(promotionOpenId)) {
                // 自己扫自己，并且表中不存在数据
                PromotionLevel promotionLevel = getDefaultPromotionLevel();
                promotionLevel.setProfileId(profile.getId());
                promotionLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(promotionLevel);
                return;
            }
            PromotionLevel promotionLevelObject = promotionLevelDao.loadByProfileId(promotionProfileId, activity); // 推广人层级表对象

            PromotionLevel promotionLevel = getDefaultPromotionLevel();
            // 若没有推广人，推广人没有扫码，或者已经是会员，则默认 level 为2
            promotionLevel.setProfileId(profile.getId());
            promotionLevel.setLevel(promotionLevelObject == null ? 2 : promotionLevelObject.getLevel() + 1);
            promotionLevel.setPromoterId(promotionProfileId);
            promotionLevelDao.insertPromotionLevel(promotionLevel);

            if (promotionLevelObject == null) {
                // 如果该推广人表中不存在，则默认添加一条
                PromotionLevel promoterLevel = getDefaultPromotionLevel();
                promoterLevel.setProfileId(promotionProfileId);
                promoterLevel.setLevel(1);
                promoterLevel.setValid(promotionProfile.getRiseMember() == 1 ? 0 : 1);
                promotionLevelDao.insertPromotionLevel(promoterLevel);
            }

            List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profile.getId(), activity);
            if (promotionActivities.size() == 0) {
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setAction(PromotionConstants.FreeLimitAction.InitState);
                promotionActivity.setActivity(PromotionConstants.Activities.FreeLimit);
                promotionActivityDao.insertPromotionActivity(promotionActivity);
            }
        }
    }

    @Override
    public void initFirstPromotionLevel(String openId, Integer riseMember) {
        // 不是会员
        if (riseMember != null && riseMember != 1) {
            Profile profile = accountService.getProfile(openId);
            List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profile.getId(), PromotionConstants.Activities.FreeLimit);
            if (promotionActivities.size() == 0) {
                // 用户行为
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setAction(PromotionConstants.FreeLimitAction.InitState);
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setActivity(PromotionConstants.Activities.FreeLimit);
                promotionActivityDao.insertPromotionActivity(promotionActivity);

                // promotionLevel 插入数据
                PromotionLevel targetPromotionLevel = getDefaultPromotionLevel();
                targetPromotionLevel.setProfileId(profile.getId());
                targetPromotionLevel.setLevel(1);
                promotionLevelDao.insertPromotionLevel(targetPromotionLevel);
            }
        }
    }

    @Override
    public void recordOrderAndSendMsg(String openId, Integer newAction) {
        Profile profile = accountService.getProfile(openId);
        List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profile.getId(), PromotionConstants.Activities.FreeLimit);

        if (promotionActivities.size() != 0) {
            // 该用户存在于新人表，并且是此次活动的用户，在活动行为表中记录此次行为
            PromotionActivity insertActivity = new PromotionActivity();
            insertActivity.setProfileId(profile.getId());
            insertActivity.setActivity(PromotionConstants.Activities.FreeLimit);
            insertActivity.setAction(newAction);
            promotionActivityDao.insertPromotionActivity(insertActivity);

            PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), activity);
            Integer promoterId = promotionLevel.getPromoterId();

            // 该用户是自己扫官方码进入，没有推广人
            if (promoterId == null) return;

            // 必是成功推广，此时给推广人发送成功推送信息
            List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(promoterId, activity);
            List<Integer> profileIds = promotionLevels.stream().map(PromotionLevel::getProfileId).collect(Collectors.toList());
            List<PromotionActivity> newUsers = promotionActivityDao.loadNewUsers(profileIds, activity);
            List<PromotionActivity> successUsers = newUsers.stream()
                    .filter(item -> PromotionConstants.FreeLimitAction.TrialCourse == item.getAction() ||
                            PromotionConstants.FreeLimitAction.PayCourse == item.getAction())
                    .filter(distinctByKey(PromotionActivity::getProfileId))
                    .collect(Collectors.toList());

            logger.info("推广成功用户人数" + successUsers.size());

            Profile sourceProfile = accountService.getProfile(promoterId);
            // 区分是否为会员
            if (sourceProfile.getRiseMember() == 1) {
                logger.info("是会员");
                // 是会员
                if (successUsers.size() <= successNum) {
                    sendNormalSuccessOrderMsg(sourceProfile.getOpenid(), openId);
                }
            } else {
                logger.info("不是会员");
                // 非会员
                if (successUsers.size() < successNum) {
                    logger.info("正常推广");
                    sendSuccessOrderMsg(sourceProfile.getOpenid(), openId, successNum - successUsers.size());
                } else if (successUsers.size() == successNum) {
                    logger.info("准备拿优惠券");
                    // 发送优惠券，Coupon 表新增数据
                    Coupon coupon = new Coupon();
                    coupon.setOpenId(sourceProfile.getOpenid());
                    coupon.setProfileId(sourceProfile.getId());
                    coupon.setAmount(50);
                    coupon.setExpiredDate(DateUtils.afterDays(new Date(), 30));
                    coupon.setDescription("奖学金");
                    coupon.setUsed(0);
                    Integer insertResult = couponDao.insertCoupon(coupon);
                    if (insertResult > 0) {
                        logger.info("准备塞优惠券");
                        // 礼品券数据保存成功，发送获得优惠券的模板消息
                        sendSuccessPromotionMsg(sourceProfile.getOpenid());
                        // 刷新优惠券缓存
                        try {
                            rabbitMQPublisher.publish("class");
                        } catch (ConnectException e) {
                            logger.error(e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendCustomerMsg(String openId) {
        Profile profile = accountService.getProfile(openId);
        //先发文字,后发图片
        customerMessageService.sendCustomerMessage(openId, "Hi，" + profile.getNickname() + "，" +
                        "你已领取限免课程：找到本质问题，减少无效努力\n\n如需继续学习，请点击下方按钮“上课啦”\n\n" +
                        "\uD83D\uDC47送你一张专属知识卡+30个好友免费学习名额，立即分享出去吧！",
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        BufferedImage bufferedImage = cardRepository.loadDefaultCardImg(profile);
        if (bufferedImage != null) {
            // 发送图片消息
            String path = TEMP_IMAGE_PATH + CommonUtils.randomString(10) + profile.getId() + ".jpg";
            String mediaId = uploadResourceService.uploadResource(bufferedImage, path);
            customerMessageService.sendCustomerMessage(openId, mediaId,
                    Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        }
    }

    /**
     * 发送成功推广信息
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessOrderMsg(String targetOpenId, String orderOpenId, Integer remainCount) {
        Profile profile = accountService.getProfile(orderOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免小课《找到本质问题，减少无效努力》\n"));
        data.put("keyword1", new TemplateMessage.Keyword("知识传播大使召集令"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        data.put("remark", new TemplateMessage.Keyword("\n感谢你对优质内容传播做出的贡献，距离50元优惠券还有" + remainCount + "个好友啦！"));
        templateMessageService.sendMessage(templateMessage);
    }

    private void sendNormalSuccessOrderMsg(String targetOpenId, String orderOpenId) {
        Profile profile = accountService.getProfile(orderOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        templateMessage.setData(data);
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免小课《找到本质问题，减少无效努力》\n"));
        data.put("keyword1", new TemplateMessage.Keyword("知识传播大使召集令"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        data.put("remark", new TemplateMessage.Keyword("\n感谢你对优质内容传播做出的贡献！"));
        templateMessageService.sendMessage(templateMessage);
    }

    /**
     * 发送获得优惠券信息
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessPromotionMsg(String targetOpenId) {
        Profile profile = accountService.getProfile(targetOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(ConfigUtils.domainName() + "/rise/static/customer/account");
        templateMessage.setTemplate_id(ConfigUtils.getReceiveCouponMsg());
        data.put("first", new TemplateMessage.Keyword("恭喜！你已将优质内容传播给" + successNum + "位好友，成功get一张¥50代金券\n"));
        data.put("keyword1", new TemplateMessage.Keyword(profile.getNickname()));
        data.put("keyword2", new TemplateMessage.Keyword("¥50代金券"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n点击下方“上课啦”并升级会员/报名小课，立即使用代金券，开学！"));
        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public Boolean hasGetTheCoupon(Integer profileId) {
        List<Coupon> coupons = couponDao.loadByProfileId(profileId);
        Long operationCouponCount = coupons.stream().filter(coupon -> coupon.getAmount().equals(50) && coupon.getDescription().equals("奖学金")).count();
        return operationCouponCount > 0;
    }

    /**
     * 获取该活动的默认 level 值，默认为生效状态
     */
    private PromotionLevel getDefaultPromotionLevel() {
        PromotionLevel promotionLevel = new PromotionLevel();
        promotionLevel.setActivity(PromotionConstants.Activities.FreeLimit);
        promotionLevel.setValid(1);
        return promotionLevel;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}