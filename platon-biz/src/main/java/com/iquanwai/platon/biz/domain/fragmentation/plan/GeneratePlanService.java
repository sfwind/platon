package com.iquanwai.platon.biz.domain.fragmentation.plan;

/**
 * Created by justin on 16/12/13.
 */
public interface GeneratePlanService {
    /**
     * 为学员生成训练计划
     *  @param openid 学员id
     *  @param problemId 问题id
     * */
    Integer generatePlan(String openid, Integer problemId);

    // 热身训练每个任务的题量
    int WARMUP_TASK_PRACTICE_NUMBER = 3;

    // 每组的热身训练数量
    int WARMUP_TASK_NUMBER = 2;
    // 每组的应用训练数量
    int APPLICATION_TASK_NUMBER = 2;
}
