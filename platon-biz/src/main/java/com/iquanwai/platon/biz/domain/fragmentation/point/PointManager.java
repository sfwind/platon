package com.iquanwai.platon.biz.domain.fragmentation.point;

import com.iquanwai.platon.biz.po.WarmupPractice;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/14.
 */
public interface PointManager {
    /**
     * 训练加分
     *
     * @param planId    训练计划id
     * @param increment 积分增幅
     */
    void risePoint(Integer planId, Integer increment);

    /**
     * 给用户信息表加分
     *
     * @param profileId 用户id
     * @param increment 积分增幅
     */
    void riseCustomerPoint(Integer profileId, Integer increment);

    /**
     * 巩固练习算分
     *
     * @param warmupPractice 巩固练习
     * @param userChoiceList 用户选项
     */
    Pair<Integer, Boolean> warmupScore(WarmupPractice warmupPractice, List<Integer> userChoiceList);

    //简单选择题分数
    int WARMUP_EASY_SCORE = 20;
    //普通选择题分数
    int WARMUP_NORMAL_SCORE = 30;
    //困难选择题分数
    int WARMUP_HARD_SCORE = 50;

    //简单选择题分数
    int APPLICATION_EASY_SCORE = 40;
    //普通选择题分数
    int APPLICATION_NORMAL_SCORE = 60;
    //困难选择题分数
    int APPLICATION_HARD_SCORE = 100;

    /**
     * 计算应用题的分数
     *
     * @param difficulty 难度
     * */
    Integer calcApplicationScore(Integer difficulty);
}
