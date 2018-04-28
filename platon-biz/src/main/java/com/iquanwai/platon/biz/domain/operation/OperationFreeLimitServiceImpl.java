package com.iquanwai.platon.biz.domain.operation;

import com.iquanwai.platon.biz.dao.common.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class OperationFreeLimitServiceImpl implements OperationFreeLimitService {
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

}
