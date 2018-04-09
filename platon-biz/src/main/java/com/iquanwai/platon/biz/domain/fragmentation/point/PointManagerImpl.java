package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
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
