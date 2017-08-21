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
        if (!StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.CourseReduction)
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
            sendMsg = "你已经领取了粉丝优惠通道：\uD83D\uDC47\n" +
                    "\n" +
                    "<a href='" + ConfigUtils.adapterDomainName() +
                    "/rise/static/plan/view?id=" +
                    problem.getId() +
                    "&free=true'>『" + problem.getProblem() + "』</a>\n" +
                    "------------\n" +
                    "P. S. 完成小课章节有神秘卡片，注意收集[机智]\n" +
                    "\n" +
                    "这里就是上课的教室，强烈建议点击右上角置顶哦~";
        } else {
            sendMsg = "你已经领取了多门课程的粉丝优惠通道：\uD83D\uDC47\n" +
                    "\n" +
                    "P. S. 完成小课章节有神秘卡片，注意收集[机智]\n" +
                    "\n" +
                    "这里就是上课的教室，强烈建议点击右上角置顶哦~";
        }

        customerMessageService.sendCustomerMessage(subscribeEvent.getOpenid(), "Hi，" + profile.getNickname() + "，" +
                        "你已领取减免课程\n\n如需继续学习，请点击下方按钮“上课啦”\n\n",
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
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

    @Override
    public void saveCourseReductionPayedLog(QuanwaiOrder quanwaiOrder) {
        Profile profile = accountService.getProfile(quanwaiOrder.getOpenid());
        Pair<CourseReductionActivity, PromotionLevel> pair = this.loadRecentCourseReduction(profile.getId(), Integer.parseInt(quanwaiOrder.getGoodsId()));
        if (pair != null) {
            logger.info("记录优惠使用情况:{}", quanwaiOrder);
            CourseReductionActivity courseReductionActivity = pair.getLeft();
            PromotionLevel promotionLevel = pair.getRight();

            PromotionActivity activity = new PromotionActivity();
            activity.setProfileId(profile.getId());
            activity.setActivity(promotionLevel.getActivity());
            if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_MEMBER)) {
                // 会员
                activity.setAction(PromotionConstants.CourseReductionAction.PayMember);
            } else if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_RISE_COURSE)) {
                // 小课
                activity.setAction(PromotionConstants.CourseReductionAction.PayCourse);
            }
            promotionActivityDao.insertPromotionActivity(activity);
        }
    }


    @Override
    public Pair<CourseReductionActivity, PromotionLevel> loadRecentCourseReduction(Integer profileId, Integer problemId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByRegex(PromotionConstants.Activities.CourseReduction, profileId);
        if (CollectionUtils.isEmpty(promotionLevels)) {
            return null;
        }
        List<String> activities = Lists.newArrayList();
        promotionLevels.forEach(level -> {
            String activity = level.getActivity();
            String[] split = activity.split("_");
            if (split.length < 2) {
                logger.error("异常，课程减免活动数据异常", level);
            } else {
                String realActivity = split[0] + "_" + split[1];
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
