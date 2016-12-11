package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;

/**
 * Created by justin on 16/12/4.
 */
public interface PlanService {
    /**
    * 为学员生成训练计划
    *  @param openid 学员id
    *  @param problemId 问题id
    * */
    Integer generatePlan(String openid, Integer problemId);

    /**
     * 获取详细的训练计划(含练习)
     * @param planId 训练计划id
     */
    ImprovementPlan getPlanDetail(Integer planId);

    /**
     * @param openid 学员id
     */
    ImprovementPlan getRunningPlan(String openid);

    /**
     * 获取简略的训练计划(不含练习)
     * @param planId 训练计划id
     */
    ImprovementPlan getPlan(Integer planId);

    /**
     * 获取知识点
     * @param knowledgeId 知识点id
     */
    Knowledge getKnowledge(Integer knowledgeId);

    void readWizard(Integer planId);

    // 热身训练每个任务的题量
    int WARMUP_TASK_PRACTICE_NUMBER = 3;
}
