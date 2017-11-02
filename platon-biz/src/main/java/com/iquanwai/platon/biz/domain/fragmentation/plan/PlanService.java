package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
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
    List<ImprovementPlan> getRunningPlan(Integer profileId);

    /**
     * 检查是否能够选新课
     *
     * @param plans  用户的小课数据
     * @return left:是否能够选小课(-1,先完成一门，-2，试用版只能完成前三节) right:提示信息
     */
    Pair<Integer, String> checkChooseNewProblem(List<ImprovementPlan> plans);

    Pair<Boolean, String> checkChooseCampProblem(Integer profileId, Integer problemId, MonthlyCampConfig monthlyCampConfig);

    void unlockCampPlan(Integer profileId, Integer planId);

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

    RiseCourseOrder getEntryRiseCourseOrder(Integer profileId, Integer problemId);

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
     */
    void completePlan(Integer planId, Integer status);


    /**
     * 结束训练计划校验
     * @param improvementPlan 训练计划
     */
    boolean completeCheck(ImprovementPlan improvementPlan);

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
     * 查询是否有该小课
     * @param profileId 用户id
     * @param problemId 小课id
     */
    ImprovementPlan getPlanByProblemId(Integer profileId, Integer problemId);

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

    /**
     * 获取用户plan，包括已删除的
     * 主要用在resolve里，判断用户是否用过rise
     */
    List<ImprovementPlan> loadUserPlans(Integer profileId);

    /**
     * 获取用户的计划列表，主要用在列表页面，会set小课头图和名字
     */
    List<ImprovementPlan> getPlanList(Integer profileId);

    /**
     * 获取当前正在学习月份的能够学习的训练营小课内容
     */
    List<ImprovementPlan> getCurrentCampPlanList(Integer profileId, MonthlyCampConfig monthlyCampConfig);

    Boolean loadChapterCardAccess(Integer profileId, Integer problemId, Integer practicePlanId);

    /**
     * 用户每学习完一章之后，弹出该章章节卡
     * @param profileId
     * @param problemId
     * @param practicePlanId
     * @return
     */
    String loadChapterCard(Integer profileId, Integer problemId, Integer practicePlanId);

    /**
     * 小课介绍页的按钮状态
     * @return
     */
    Integer problemIntroductionButtonStatus(Integer profileId, Boolean isMember, Integer problemId, ImprovementPlan plan, Boolean autoOpen);

    void forceOpenCampOrder(String orderId, MonthlyCampConfig monthlyCampConfig);

    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date closeDate, Boolean sendWelcomeMsg);

    Integer magicUnlockProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg);

    Integer forceOpenProblem(Integer profileId, Integer problemId, Date closeDate);

    /**
     * 小课强开
     * startDate 小课开始日期
     * closeDate 小课关闭日期
     */
    Integer forceOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate);

    Pair<Boolean, String> loadProblemChooseAccess(Integer profileId);

    AuditionClassMember loadAuditionClassMember(Integer profileId);

    Integer insertAuditionClassMember(AuditionClassMember auditionClassMember);

    Integer setAuditionOpened(Integer id);
}
