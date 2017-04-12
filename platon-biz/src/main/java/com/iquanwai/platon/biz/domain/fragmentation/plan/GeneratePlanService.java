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

    // 每节的应用训练数量
    int APPLICATION_TASK_NUMBER = 2;

    int KNOWLEDGE_SEQUENCE = 1; //知识点顺序
    int WARMUP_SEQUENCE = 2; //巩固训练顺序

    int PROBLEM_MAX_LENGTH = 30; //专题最长开放时间
}
