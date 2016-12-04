package com.iquanwai.platon.biz.domain.practice;

/**
 * Created by justin on 16/12/4.
 */
public interface PlanService {
    /**
    * 为学员生成训练计划
    *  @param openid 学员id
    *  @param problemId 问题id
    * */
    void generatePlan(String openid, Integer problemId);
}
