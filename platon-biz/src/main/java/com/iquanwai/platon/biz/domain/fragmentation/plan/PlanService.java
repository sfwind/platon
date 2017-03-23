package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.WarmupPractice;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
public interface PlanService {

    /**
     * 构建详细的训练计划
     * @param improvementPlan 训练计划
     */
    void buildPlanDetail(ImprovementPlan improvementPlan);

    /**
     * 构建详细的某组训练计划
     * @param improvementPlan 训练计划
     * @param series 训练组号
     * @return 0-已组装,-1-已到最后一组
     */
    Integer buildSeriesPlanDetail(ImprovementPlan improvementPlan, Integer series);

    /**
     * 获取学员进行中的训练
     * @param openid 学员id
     */
    ImprovementPlan getRunningPlan(String openid);

    /**
     * 获取学员最近的训练
     * @param openid 学员id
     */
    ImprovementPlan getLatestPlan(String openid);

    List<ImprovementPlan> getPlans(String openid);

    /**
     * 获取所有正在进行的训练
     */
    List<ImprovementPlan> loadAllRunningPlan();

    /**
     * 获取简略的训练计划(不含练习)
     * @param planId 训练计划id
     */
    ImprovementPlan getPlan(Integer planId);

    /**
     * 更新钥匙数量
     * @param planId 训练计划id
     * @param key 钥匙数量
     */
    void updateKey(Integer planId, Integer key);

    /**
     * 获取知识点
     * @param knowledgeId 知识点id
     */
    Knowledge getKnowledge(Integer knowledgeId);

    /**
     * 学习知识点
     * @param knowledgeId 知识点id
     * @param planId 训练计划id
     */
    void learnKnowledge(Integer knowledgeId, Integer planId);

    /**
     * 训练计划结束
     * @param planId 训练计划id
     * @param status 训练状态
     */
    void completePlan(Integer planId, Integer status);

    /**
     * 结束训练计划校验
     * @param improvementPlan 训练计划
     */
    boolean completeCheck(ImprovementPlan improvementPlan);

    /**
     * 获取下一个训练项目
     * @param practicePlanId 当前训练id
     * */
    Practice nextPractice(Integer practicePlanId);

    /**
     * 获取例题
     * @param knowledgeId 知识点id
     * @param problemId 专题id
     * */
    WarmupPractice getExample(Integer knowledgeId, Integer problemId);

    /**
     * 当前组练习是否可以做
     * @param series 组序号
     * @param improvementPlan 训练计划
     * @return -1 未解锁
     * @return -2 之前系列未完成
     * */
    Integer checkPractice(Integer series, ImprovementPlan improvementPlan);

    /**
     * 查询是否有该专题
     * @param openId 用户id
     * @param problemId 专题id
     */
    boolean hasProblemPlan(String openId,Integer problemId);

    /**
     * 获取专题介绍
     * @param problemId 专题id
     */
    String loadSubjectDesc(Integer problemId);

}
