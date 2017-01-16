package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.iquanwai.platon.biz.po.WarmupPractice;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/14.
 */
public interface PointRepo {
    /**
     * 训练加分
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

    int EASY_SCORE = 20;
    int NORMAL_SCORE = 30;
    int HARD_SCORE = 50;
}
