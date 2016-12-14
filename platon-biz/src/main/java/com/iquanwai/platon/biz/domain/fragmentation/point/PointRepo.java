package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.iquanwai.platon.biz.po.WarmupPractice;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/14.
 */
public interface PointRepo {
    /**
     * 提交挑战训练
     * @param planId 训练计划id
     * @param increment 积分增幅
     * */
    void risePoint(Integer planId, Integer increment);

    /**
     * 热身训练算分
     * @param warmupPractice 热身训练
     * @param userChoiceList 用户选项
     * */
    Pair<Integer, Boolean> warmupScore(WarmupPractice warmupPractice, List<Integer> userChoiceList);

    //单选题得分
    int RADIO_PRACTICE_SCORE = 20;
    //多选题得分
    int MULTI_CHOICE_PRACTICE_SCORE = 50;
    //挑战训练得分
    int CHALLENGE_PRACTICE_SCORE = 500;
}
