package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.po.Choice;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.WarmupPractice;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/14.
 */
@Service
public class PointRepoImpl implements PointRepo {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Override
    public void risePoint(Integer planId, Integer increment) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(improvementPlan!=null){
            improvementPlanDao.updatePoint(planId, improvementPlan.getPoint()+increment);
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
            if(warmupPractice.getType()== PracticePlan.RADIO){
                return new ImmutablePair<>(RADIO_PRACTICE_SCORE, true);
            }else if(warmupPractice.getType()==PracticePlan.MULTIPLE_CHOICE){
                return new ImmutablePair<>(MULTI_CHOICE_PRACTICE_SCORE, true);
            }
        }
        return new ImmutablePair<>(0, false);
    }
}
