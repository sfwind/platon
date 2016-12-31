package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;

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
     * 获取学员进行中的训练
     * @param openid 学员id
     */
    ImprovementPlan getRunningPlan(String openid);

    /**
     * 获取学员最近的训练
     * @param openid 学员id
     */
    ImprovementPlan getLatestPlan(String openid);

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
     * 训练计划结束
     * @param planId 训练计划id
     */
    void completePlan(Integer planId);

    /**
     * 获取下一个训练项目
     * @param improvementPlan 训练计划
     * */
    Practice nextPractice(ImprovementPlan improvementPlan);

}
