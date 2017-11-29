package com.iquanwai.platon.biz.domain.fragmentation.plan;

import java.util.Date;

/**
 * Created by justin on 16/12/13.
 */
public interface GeneratePlanService {
    // 每节的应用练习数量
    int APPLICATION_TASK_NUMBER = 2;
    //知识点顺序
    int KNOWLEDGE_SEQUENCE = 1;
    //巩固练习顺序
    int WARMUP_SEQUENCE = 2;
    //小课最长开放时间
    int PROBLEM_MAX_LENGTH = 30;

    /**
     * 强制将该 ImprovementPlan 重开
     * @param planId 计划id
     */
    void forceReopenPlan(Integer planId);

    /**
     * 为学员生成训练计划
     *  @param profileId 学员id
     *  @param problemId 问题id
     * */
    Integer generatePlan(Integer profileId, Integer problemId);

    /**
     * 发送开课通知
     *  @param openid 学员id
     *  @param problemId 问题id
     * */
    void sendOpenPlanMsg(String openid, Integer problemId);

    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date closeDate, Boolean sendWelcomeMsg);

    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg);

    /**
     * 小课强开
     * startDate 小课开始日期
     * closeDate 小课关闭日期
     */
    Integer forceOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate);
}
