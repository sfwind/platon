package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointManagerImpl implements PointManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private CacheService cacheService;

    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (improvementPlan != null) {
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint() + increment);
            Integer profileId = improvementPlan.getProfileId();
            Profile profile = profileDao.load(Profile.class, profileId);
            if (profile != null) {
                profileDao.updatePoint(profileId, profile.getPoint() + increment);
            } else {
                logger.error("用户{} 加{}积分失败,缺少Profile记录", profileId, increment);
            }
        } else {
            logger.error("计划{} 加{}积分失败，缺少Plan记录", planId, increment);
        }
    }

    @Override
    public void riseCustomerPoint(Integer profileId, Integer increment) {
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile != null) {
            profileDao.updatePoint(profileId, profile.getPoint() + increment);
        } else {
            logger.error("用户{} 加{}积分失败,缺少Profile记录", profileId, increment);
        }
    }

    @Override
    public Pair<Integer, Boolean> warmupScore(WarmupPractice warmupPractice, List<Integer> userChoiceList) {
        Assert.notNull(warmupPractice, "练习不能为空");

        List<Choice> all = warmupPractice.getChoiceList();
        List<Choice> right = Lists.newArrayList();
        right.addAll(all.stream().filter(Choice::getIsRight).
                collect(Collectors.toList()));

        for (Choice choice : right) {
            if (!userChoiceList.contains(choice.getId())) {
                return new ImmutablePair<>(0, false);
            }
        }

        if (right.size() == userChoiceList.size()) {
            int score = calcWarmupScore(warmupPractice.getDifficulty());
            if (score > 0) {
                return new ImmutablePair<>(score, true);
            }
        }
        return new ImmutablePair<>(0, false);
    }

    @Override
    public Integer warmupPoint(Integer planId, Integer profileId) {
        // 选择题满分30分,按照题目的难易度算出加权分数
        List<WarmupSubmit> warmupSubmits = warmupSubmitDao.getWarmupSubmits(planId, profileId);
        // 获取回答正确的选择题id
        List<Integer> rightQuestions = warmupSubmits.stream().filter(WarmupSubmit::getIsRight)
                .map(WarmupSubmit::getQuestionId)
                .collect(Collectors.toList());

        List<PracticePlan> practicePlans = practicePlanDao.loadWarmupPracticeByPlanId(planId);

        List<Integer> allWarmupPractices = Lists.newArrayList();

        // 解析课程所有的选择题id
        practicePlans.forEach(practicePlan -> {
            String practiceId = practicePlan.getPracticeId();
            String[] warmupIds = practiceId.split(",");
            for (String warmupId : warmupIds) {
                allWarmupPractices.add(Integer.valueOf(warmupId));
            }
        });

        //选择题总分
        int sumScore = 0;
        //选择题正确得分
        int rightScore = 0;

        for (Integer warmupId : allWarmupPractices) {
            WarmupPractice warmupPractice = cacheService.getWarmupPractice(warmupId);
            sumScore = sumScore + calcWarmupScore(warmupPractice.getDifficulty());

            if (rightQuestions.contains(warmupId)) {
                rightScore = rightScore + calcWarmupScore(warmupPractice.getDifficulty());
            }
        }

        return WARMUP_TOTAL * rightScore / sumScore;
    }

    @Override
    public Integer applicationPoint(Integer planId, Integer profileId) {
        // 思考题满分20分,按照题目的难易度算出加权分数
        List<PracticePlan> practicePlans = practicePlanDao.loadApplicationPracticeByPlanId(planId);
        List<Integer> applicationIds = practicePlans.stream().filter(practicePlan ->
                practicePlan.getType() != PracticePlan.APPLICATION_GROUP)
                .map(PracticePlan::getId)
                .collect(Collectors.toList());

        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.
                loadApplicationSubmitsByApplicationIds(applicationIds, planId);

        //是否有被加精的作业
        boolean highlight = applicationSubmits.stream().filter(applicationSubmit -> applicationSubmit.getPriority() > 0)
                .count() > 0;

        List<Integer> completeApplicationIds = applicationSubmits.stream().filter(applicationSubmit ->
                applicationSubmit.getLength() > 10 ||
                        applicationSubmit.getHasImage()).map(ApplicationSubmit::getApplicationId)
                .collect(Collectors.toList());

        List<ApplicationPractice> applicationPractices = applicationPracticeDao.loadPracticeList(applicationIds);

        //应用题总分
        int sumScore = 0;
        //应用题完成得分
        int completeScore = 0;

        for (ApplicationPractice applicationPractice : applicationPractices) {
            sumScore = sumScore + calcApplicationScore(applicationPractice.getDifficulty());

            if (completeApplicationIds.contains(applicationPractice.getId())) {
                completeScore = completeScore + calcApplicationScore(applicationPractice.getDifficulty());
            }
        }

        //答题分数+加精分数
        return APPLICATION_TOTAL * completeScore / sumScore + (highlight ? APPLICATION_HIGHLIGHT : 0);
    }


    private Integer calcWarmupScore(Integer difficulty) {
        if (difficulty == 1) {
            return WARMUP_EASY_SCORE;
        } else if (difficulty == 2) {
            return WARMUP_NORMAL_SCORE;
        } else if (difficulty == 3) {
            return WARMUP_HARD_SCORE;
        }

        return 0;
    }

    @Override
    public Integer calcApplicationScore(Integer difficulty) {
        if (difficulty == 1) {
            return APPLICATION_EASY_SCORE;
        } else if (difficulty == 2) {
            return APPLICATION_NORMAL_SCORE;
        } else if (difficulty == 3) {
            return APPLICATION_HARD_SCORE;
        }

        return 0;
    }

}
