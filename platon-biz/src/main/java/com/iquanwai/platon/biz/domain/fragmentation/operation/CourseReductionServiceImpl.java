package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.CourseReductionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.CourseReductionActivity;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CourseReductionActivityDao courseReductionActivityDao;
    @Autowired
    private CacheService cacheService;

    @Override
    public void scanCourseReductionQR(SubscribeEvent subscribeEvent) {
        logger.info("处理课程优惠mq;{}", subscribeEvent);
        if (!StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.COURSE_REDUCTION)
                || subscribeEvent.getOpenid() == null) {
            logger.error("扫描优惠推广课程的事件处理异常,{}", subscribeEvent);
            return;
        }
        Profile profile = accountService.getProfile(subscribeEvent.getOpenid());
        if (profile == null) {
            logger.error("扫描推广课程的事件处理异常，没有该用户:{}", subscribeEvent.getOpenid());
            return;
        }
        List<String> list = Lists.newArrayList();
        list.add(subscribeEvent.getScene().split("_")[0]);
        CourseReductionActivity activity = courseReductionActivityDao.loadReductions(list).stream().findFirst().orElse(null);
        if (activity == null) {
            logger.error("没有活动信息:{}", subscribeEvent);
            return;
        }
        String sendMsg;
        if (activity.getProblemId() != null) {
            Problem problem = cacheService.getProblem(activity.getProblemId());

            sendMsg = profile.getNickname() + "，果然只有机智的人才能成为张良计的真爱粉[嘿哈]\n" +
                    "\n" +
                    "赶紧点击下方链接购买\uD83D\uDC47\n" +
                    "\n" +
                    "<a href='" + ConfigUtils.adapterDomainName() +
                    "/rise/static/plan/view?id=" +
                    problem.getId() +
                    "&free=true'>『" + problem.getProblem() + "』</a>\n";
        } else {
            sendMsg = profile.getNickname() + "，果然只有机智的人才能成为张良计的真爱粉[嘿哈]\n" +
                    "点击\"上课啦\" 进入发现页面购买课程吧！";
        }

        customerMessageService.sendCustomerMessage(subscribeEvent.getOpenid(), sendMsg,
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        // 发送海报
//        String mediaId = ConfigUtils.isDevelopment() == null || !ConfigUtils.isDevelopment() ? "oNP9rE2TKhmfaLkbdss_lK5OuF5bADXqYrx1wjyFWVE" : "DKejbjbUawA773Mq37YnIRIHbTMlMEQT_WTTuWYab4M17KELKS6Cwtguk5pLWnS4";
//        customerMessageService.sendCustomerMessage(subscribeEvent.getOpenId(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        //直接入activity
        PromotionActivity promotionActivity = new PromotionActivity();
        promotionActivity.setAction(PromotionConstants.CourseReductionAction.SCAN_CODE);
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

    @Override
    public void saveCourseReductionPayedLog(QuanwaiOrder quanwaiOrder) {
        Profile profile = accountService.getProfile(quanwaiOrder.getProfileId());
        Pair<CourseReductionActivity, PromotionLevel> pair = this.loadRecentCourseReduction(profile.getId(), Integer.parseInt(quanwaiOrder.getGoodsId()));
        if (pair != null) {
            logger.info("记录优惠使用情况:{}", quanwaiOrder);
            PromotionLevel promotionLevel = pair.getRight();

            PromotionActivity activity = new PromotionActivity();
            activity.setProfileId(profile.getId());
            activity.setActivity(promotionLevel.getActivity());
            if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_MEMBER)) {
                // 会员
                activity.setAction(PromotionConstants.CourseReductionAction.PAY_MEMBER);
            } else if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_COURSE)) {
                // 购买课程
                activity.setAction(PromotionConstants.CourseReductionAction.PAY_COURSE);
            }
            promotionActivityDao.insertPromotionActivity(activity);
        }
    }



    @Override
    public Pair<CourseReductionActivity, PromotionLevel> loadRecentCourseReduction(Integer profileId, Integer problemId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByRegex(PromotionConstants.Activities.COURSE_REDUCTION, profileId);
        if (CollectionUtils.isEmpty(promotionLevels)) {
            return null;
        }
        List<String> activities = Lists.newArrayList();
        promotionLevels.forEach(level -> {
            String activity = level.getActivity();
            String[] split = activity.split("_");
            if (split.length < 1) {
                logger.error("异常，课程减免活动数据异常", level);
            } else {
                String realActivity = split[0];
                if (!activities.contains(realActivity)) {
                    activities.add(realActivity);
                }
            }
        });
        List<CourseReductionActivity> list = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(activities)) {
            list = courseReductionActivityDao
                    .loadReductions(activities)
                    .stream()
                    .filter(activity -> activity.getProblemId() == null ||
                            activity.getProblemId().equals(problemId))
                    .collect(Collectors.toList());
        }
        promotionLevels.sort((o1, o2) -> o2.getId() - o1.getId());
        for (PromotionLevel level : promotionLevels) {
            for (CourseReductionActivity courseReductionActivity : list) {
                if (level.getActivity().contains(courseReductionActivity.getActivity())) {
                    // 取出第一个匹配到的courseReduction,因为有可能扫码后活动取消了
                    // courseReduction_zlj_02 contains courseReduction_zlj
                    return new MutablePair<>(courseReductionActivity, level);
                }
            }
        }
        // 上面没有return的话，则return null
        return null;
    }
}
