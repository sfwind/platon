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
    //课程最长开放时间
    int PROBLEM_MAX_LENGTH = 30;

    /**
     * 强制将该 ImprovementPlan 重开,延长30天有效期
     *
     * @param planId 计划id
     */
    void forceReopenPlan(Integer planId);

    /**
     * 创建一带二活动计划
     *
     * @param profileId ProfileId
     * @return 计划id
     */
    Integer createTeamLearningPlan(Integer profileId);

    /**
     * 生成学习计划
     *
     * @param profileId profileId
     * @param problemId 小课id
     * @param maxSeries 最大学习节数
     * @param startDate 开始时间
     * @param closeDate 结束时间
     * @return 小课id
     */
    Integer generatePlan(Integer profileId, Integer problemId, Integer maxSeries, Date startDate, Date closeDate);

    /**
     * 为学员生成训练计划
     *
     * @param profileId 学员id
     * @param problemId 问题id
     * @return 小课id
     */
    Integer generatePlan(Integer profileId, Integer problemId);

    /**
     * 发送开课通知
     *
     * @param openid    学员id
     * @param problemId 问题id
     */
    void sendOpenPlanMsg(String openid, Integer problemId);

    /**
     * 解锁下一节，并设置为进行中
     *
     * @param profileId profileId
     * @param problemId 小课id
     * @param closeDate 关闭时间
     * @return 小课id
     */
    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date closeDate);

    /**
     * 解锁下一节，并设置为进行中
     *
     * @param profileId profileId
     * @param problemId 小课id
     * @param startDate 开始时间
     * @param closeDate 结束时间
     * @return 小课id
     */
    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate);

    /**
     * 有课解锁，没课开课，不强制解锁
     *
     * @param profileId      用户id
     * @param problemId      问题id
     * @param startDate      开课时间
     * @param closeDate      关闭时间
     * @param sendWelcomeMsg 第一次开课时，是否发送模板消息
     * @return 小课id
     */
    Integer magicOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg);

    /**
     * 强制开课,解锁所有章节
     * <p>已经开过课，则永远不发模板消息</p>
     *
     * @param profileId      用户id
     * @param problemId      问题id
     * @param startDate      开课时间
     * @param closeDate      关闭时间
     * @param sendWelcomeMsg 第一次开课时，是否发送模板消息
     * @return 小课id
     */
    Integer forceOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg);

    /**
     * 课程强开,练习强行解锁
     *
     * @param profileId 用户id
     * @param problemId 问题id
     * @param startDate 课程开始日期
     * @param closeDate 课程关闭日期
     * @return 小课id
     */
    Integer forceOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate);
}
