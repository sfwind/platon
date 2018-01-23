package com.iquanwai.platon.biz.domain.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 推广的熊猫卡临时存放路径
    private final static String TEMP_IMAGE_PATH = "/data/static/images/";
    // 推广成功人数限额
    private final static Integer SUCCESS_NUM = ConfigUtils.getFreeLimitSuccessCnt();
    // 推广活动名称
    private final static String ACTIVITY = PromotionConstants.Activities.FREE_LIMIT;

    @PostConstruct
    public void init() {
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

        PromotionLevel tempPromotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), ACTIVITY);
        if (tempPromotionLevel != null) {
            return; // 不是本次活动，或者说已被其他用户推广则不算新人
        }

        String[] sceneParams = scene.split("_");
        if (profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
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
            PromotionLevel promotionLevelObject = promotionLevelDao.loadByProfileId(promotionProfileId, ACTIVITY); // 推广人层级表对象

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
                promoterLevel.setValid(promotionProfile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP ? 0 : 1);
                promotionLevelDao.insertPromotionLevel(promoterLevel);
            }

            List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profile.getId(), ACTIVITY);
            if (promotionActivities.size() == 0) {
                PromotionActivity promotionActivity = new PromotionActivity();
                promotionActivity.setProfileId(profile.getId());
                promotionActivity.setAction(PromotionConstants.FreeLimitAction.INIT_STATE);
                promotionActivity.setActivity(PromotionConstants.Activities.FREE_LIMIT);
                promotionActivityDao.insertPromotionActivity(promotionActivity);
            }
        }
    }

    @Override
    public void recordOrderAndSendMsg(String openId, Integer newAction) {
        Profile profile = accountService.getProfile(openId);
        List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profile.getId(), PromotionConstants.Activities.FREE_LIMIT);

        if (promotionActivities.size() != 0) {
            // 该用户存在于新人表，并且是此次活动的用户，在活动行为表中记录此次行为
            PromotionActivity insertActivity = new PromotionActivity();
            insertActivity.setProfileId(profile.getId());
            insertActivity.setActivity(PromotionConstants.Activities.FREE_LIMIT);
            insertActivity.setAction(newAction);
            promotionActivityDao.insertPromotionActivity(insertActivity);

            PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), ACTIVITY);
            Integer promoterId = promotionLevel.getPromoterId();

            // 该用户是自己扫官方码进入，没有推广人
            if (promoterId == null) {
                return;
            }

            // 必是成功推广，此时给推广人发送成功推送信息
            List<PromotionLevel> promotionLevels = promotionLevelDao.loadByPromoterId(promoterId, ACTIVITY);
            List<Integer> profileIds = promotionLevels.stream().map(PromotionLevel::getProfileId).collect(Collectors.toList());
            List<PromotionActivity> newUsers = promotionActivityDao.loadNewUsers(profileIds, ACTIVITY);
            List<PromotionActivity> successUsers = newUsers.stream()
                    .filter(item -> PromotionConstants.FreeLimitAction.TRIAL_COURSE == item.getAction() ||
                            PromotionConstants.FreeLimitAction.PAY_COURSE == item.getAction())
                    .filter(distinctByKey(PromotionActivity::getProfileId))
                    .collect(Collectors.toList());

            logger.info("推广成功用户人数" + successUsers.size());

            Profile sourceProfile = accountService.getProfile(promoterId);
            // 区分是否为会员
            if (sourceProfile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
                // 是会员
                if (successUsers.size() <= SUCCESS_NUM) {
                    sendNormalSuccessOrderMsg(sourceProfile.getOpenid(), openId);
                }
            } else {
                // 非会员
                if (successUsers.size() < SUCCESS_NUM) {
                    sendSuccessOrderMsg(sourceProfile.getOpenid(), openId, SUCCESS_NUM - successUsers.size());
                } else if (successUsers.size() == SUCCESS_NUM) {
                    // 发送优惠券，Coupon 表新增数据
                    Coupon coupon = new Coupon();
                    coupon.setProfileId(sourceProfile.getId());
                    coupon.setAmount(50);
                    coupon.setExpiredDate(DateUtils.afterDays(new Date(), 30));
                    coupon.setDescription("推广奖学金");
                    coupon.setUsed(0);
                    Integer insertResult = couponDao.insertCoupon(coupon);
                    if (insertResult > 0) {
                        // 礼品券数据保存成功，发送获得优惠券的模板消息
                        sendSuccessPromotionMsg(sourceProfile.getOpenid());
                    }
                }
            }
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
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免课程《找到本质问题，减少无效努力》\n"));
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
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免课程《找到本质问题，减少无效努力》\n"));
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
        data.put("first", new TemplateMessage.Keyword("恭喜！你已将优质内容传播给" + SUCCESS_NUM + "位好友，成功get一张¥50代金券\n"));
        data.put("keyword1", new TemplateMessage.Keyword(profile.getNickname()));
        data.put("keyword2", new TemplateMessage.Keyword("¥50代金券"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n点击下方“上课啦”并升级会员/报名课程，立即使用代金券，开学！"));
        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public Boolean hasGetTheCoupon(Integer profileId) {
        List<Coupon> coupons = couponDao.loadByProfileId(profileId);
        Long operationCouponCount = coupons.stream().filter(coupon -> coupon.getAmount().equals(50) &&
                "推广奖学金".equals(coupon.getDescription())).count();
        return operationCouponCount > 0;
    }

    /**
     * 获取该活动的默认 level 值，默认为生效状态
     */
    private PromotionLevel getDefaultPromotionLevel() {
        PromotionLevel promotionLevel = new PromotionLevel();
        promotionLevel.setActivity(PromotionConstants.Activities.FREE_LIMIT);
        promotionLevel.setValid(1);
        return promotionLevel;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = Maps.newConcurrentMap();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
