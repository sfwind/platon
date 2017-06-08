package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import org.apache.commons.lang3.tuple.Pair;

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
     * @param profileId 学员id
     */
    ImprovementPlan getRunningPlan(Integer profileId);

    /**
     * 获取学员最近的训练
     * @param profileId 学员id
     */
    ImprovementPlan getLatestPlan(Integer profileId);

    /**
     * 获取学员所有的训练
     * @param profileId 学员id
     */
    List<ImprovementPlan> getPlans(Integer profileId);

    /**
     * 获取简略的训练计划(不含练习)
     * @param planId 训练计划id
     */
    ImprovementPlan getPlan(Integer planId);

    /**
     * 获取章节信息
     * @param plan 训练计划
     */
    List<ProblemSchedule> getChapterList(ImprovementPlan plan);

    /**
     * 获取知识点
     * @param knowledgeId 知识点id
     */
    Knowledge getKnowledge(Integer knowledgeId);

    /**
     * 训练计划结束
     * @param planId 训练计划id
     * @param status 训练状态
     * @return 打败了多少用户
     */
    Integer completePlan(Integer planId, Integer status);

    /**
     * 结束训练计划校验
     * @param improvementPlan 训练计划
     */
    Pair<Boolean, Integer> completeCheck(ImprovementPlan improvementPlan);

    /**
     * 当前节练习是否可以做
     * @param series 节序号
     * @param improvementPlan 训练计划
     * @return -1 非会员未解锁
     * @return -2 之前系列未完成
     * @return -3 小课已过期
     * */
    Integer checkPractice(Integer series, ImprovementPlan improvementPlan);

    /**
     * 查询是否有该小课
     * @param profileId 用户id
     * @param problemId 小课id
     */
    boolean hasProblemPlan(Integer profileId, Integer problemId);

    /**
     * 获取小课介绍
     * @param problemId 小课id
     */
    String loadSubjectDesc(Integer problemId);

    /**
     * 根据训练id获取知识点路线图
     * @param problemId 小课id
     */
    List<Chapter> loadRoadMap(Integer problemId);

    /**
     * 判断小课是否完成,当理解练习和巩固练习都完成时,小课判定为完成
     * @param practicePlanId 练习id
     */
    void checkPlanComplete(Integer practicePlanId);

    /**
     * 查看是否可以完成训练
     * @param plan 计划
     * @return left:是否完成全部必做
     *
     *  right:最小完成天数,0-已过，1+未过
     */
    Pair<Boolean,Integer> checkCloseable(ImprovementPlan plan);

    /**
     * 记录用户当前所进行的小节序号
     * @param planId 训练id
     * @param series 第几小节
     */
    void markPlan(Integer series, Integer planId);

}
