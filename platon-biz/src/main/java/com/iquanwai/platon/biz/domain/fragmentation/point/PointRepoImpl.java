package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.customer.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.po.Choice;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointRepoImpl implements PointRepo {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProfileDao profileDao;

    public static Map<Integer, Integer> score = Maps.newHashMap();


    @PostConstruct
    public void initPoint() {
        List<Integer> scores = ConfigUtils.getWorkScoreList();
        logger.info("score init");
        for (int i = 0; i < scores.size(); i++) {
            score.put(i + 1, scores.get(i));
        }
        logger.info("score map:{}", score);
    }

    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(improvementPlan!=null){
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint()+increment);
        } else {
            logger.error("计划{} 加{}积分失败，缺少Plan记录",planId,increment);
        }
    }

    @Override
    public void riseCustomerPoint(String openId, Integer increment){
        Profile profile = profileDao.queryByOpenId(openId);
        if(profile!=null){
            profileDao.updatePoint(openId,profile.getPoint() + increment);
        } else {
            logger.error("用户{} 加{}积分失败,缺少Profile记录",openId,increment);
        }
    }

    public Pair<Integer, Boolean> warmupScore(WarmupPractice warmupPractice, List<Integer> userChoiceList) {
        Assert.notNull(warmupPractice, "练习不能为空");

        List<Choice> all = warmupPractice.getChoiceList();
        List<Choice> right = Lists.newArrayList();
        right.addAll(all.stream().filter(Choice::getIsRight).
                collect(Collectors.toList()));

        for(Choice choice:right){
            if(!userChoiceList.contains(choice.getId())) {
                return new ImmutablePair<>(0, false);
            }
        }

        if(right.size()==userChoiceList.size()){
            if(warmupPractice.getDifficulty()== 1){
                return new ImmutablePair<>(EASY_SCORE, true);
            }else if(warmupPractice.getDifficulty()== 2){
                return new ImmutablePair<>(NORMAL_SCORE, true);
            }else if(warmupPractice.getDifficulty()== 3){
                return new ImmutablePair<>(HARD_SCORE, true);
            }
        }
        return new ImmutablePair<>(0, false);
    }
}
