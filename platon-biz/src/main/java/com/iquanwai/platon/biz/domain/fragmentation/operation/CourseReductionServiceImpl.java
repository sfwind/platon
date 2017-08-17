package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nethunder on 2017/8/16.
 */
@Service
public class CourseReductionServiceImpl implements CourseReductionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;

    @Override
    public void scanCourseReductionQR(SubscribeEvent subscribeEvent) {
        if (!StringUtils.contains(subscribeEvent.getScene(), PromotionConstants.Activities.CourseReduction)
                || subscribeEvent.getOpenid() == null) {
            logger.error("扫描优惠推广课程的事件处理异常,{}", subscribeEvent);
            return;
        }
        Profile profile = accountService.getProfile(subscribeEvent.getOpenid());
        if (profile == null) {
            logger.error("扫描推广课程的事件处理异常，没有该用户:{}", subscribeEvent.getOpenid());
            return;
        }
        //直接入activity
        PromotionActivity promotionActivity = new PromotionActivity();
        promotionActivity.setAction(PromotionConstants.CourseReductionAction.ScanCode);
        promotionActivity.setActivity(subscribeEvent.getScene());
        promotionActivity.setProfileId(profile.getId());
        promotionActivityDao.insertPromotionActivity(promotionActivity);
        // 查看在不在level表
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), subscribeEvent.getScene());
        if (promotionLevel == null) {
            // level表没有该数据
            PromotionLevel level = new PromotionLevel();
            level.setProfileId(profile.getId());
            level.setActivity(subscribeEvent.getScene());
            level.setLevel(1);
            level.setPromoterId(null);
            level.setValid(1);
            promotionLevelDao.insertPromotionLevel(level);
        }
    }
}
