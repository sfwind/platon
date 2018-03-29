package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PracticePlanStatusManagerImpl implements PracticePlanStatusManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private OperationLogService operationLogService;

    private void tracePracticeComplete(Integer profileId, PracticePlan practicePlan) {
        switch (practicePlan.getType()) {
            case PracticePlan.WARM_UP:
            case PracticePlan.WARM_UP_REVIEW: {
                operationLogService.trace(profileId, "submitWarmupGroup", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    // 完成一组选择题
                    ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
                    boolean exist = practicePlanDao.loadWarmupPracticeByPlanId(practicePlan.getPlanId())
                            .stream()
                            .filter(item -> item.getId() != practicePlan.getId())
                            .anyMatch(item -> item.getSeries().equals(practicePlan.getSeries()) && item.getStatus() == 1);
                    prop.add("series", practicePlan.getSeries());
                    prop.add("sequence", practicePlan.getSequence());
                    prop.add("isReview", practicePlan.getType().equals(PracticePlan.WARM_UP_REVIEW));
                    prop.add("isSeriesFirst", !exist);
                    prop.add("problemId", plan.getProblemId());
                    return prop;
                });
                break;
            }
            case PracticePlan.APPLICATION_BASE:
            case PracticePlan.APPLICATION_UPGRADED: {
                operationLogService.trace(profileId, "submitApplication", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
                    boolean exist = practicePlanDao.loadApplicationPracticeByPlanId(practicePlan.getPlanId())
                            .stream()
                            .filter(item -> item.getId() != practicePlan.getId())
                            .anyMatch(item -> item.getSeries().equals(practicePlan.getSeries()) && item.getStatus() == 1);
                    prop.add("isSeriesFirst", !exist);
                    prop.add("series", practicePlan.getSeries());
                    prop.add("sequence", practicePlan.getSequence());
                    prop.add("problemId", plan.getProblemId());
                    prop.add("applicationId", practicePlan.getPracticeId());
                    prop.add("isUpgraded", practicePlan.getType().equals(PracticePlan.APPLICATION_UPGRADED));
                    return prop;
                });
                break;
            }
            case PracticePlan.CHALLENGE: {
                operationLogService.trace(profileId, "submitChallenge", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
                    prop.add("problemId", plan.getProblemId());
                    return prop;
                });
                break;
            }
            case PracticePlan.KNOWLEDGE:
            case PracticePlan.KNOWLEDGE_REVIEW: {
                operationLogService.trace(profileId, "learnKnowledge", () -> {
                    ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
                    return OperationLogService
                            .props()
                            .add("problemId", plan.getProblemId())
                            .add("series", practicePlan.getSeries())
                            .add("isReview", practicePlan.getType().equals(PracticePlan.KNOWLEDGE_REVIEW));
                });
                break;
            }
            case PracticePlan.INTRODUCTION: {
                operationLogService.trace(profileId, "learnIntroduction", () -> {
                    ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
                    return OperationLogService
                            .props()
                            .add("problemId", plan.getProblemId())
                            .add("series", practicePlan.getSeries());
                });
                break;
            }
            default: {
                // ignore
                logger.error("该题目未打点 :{}", practicePlan);
            }
        }
    }

    @Override
    public void completePracticePlan(Integer profileId, Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return;
        }

        // 学习计划 ImprovementPlan 的 id
        int planId = practicePlan.getPlanId();

        // 核实人员信息、并且将 status 改成 1，将该条记录置成完成
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        ImprovementPlan improvementPlan = improvementPlans.stream()
                .filter(plan -> plan.getId() == planId).findAny().orElse(null);
        if (improvementPlan != null) {
            // 神策打点
            tracePracticeComplete(profileId, practicePlan);
            practicePlanDao.complete(practicePlanId);
        } else {
            //没找到课程
            logger.error("{} 不是 {} 的小课", practicePlanId, profileId);
            return;
        }

        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
            //课程已关闭,不能解锁
            return;
        }

        int type = practicePlan.getType(); // 当前题目类型
        int series = practicePlan.getSeries(); // 当前小节数

        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(practicePlan.getPlanId());

        operationLogService.trace(profileId, "completePractice", () -> {
            OperationLogService.Prop prop = OperationLogService.props();
            prop.add("problemId", improvementPlan.getProblemId());
            prop.add("series", practicePlan.getSeries());
            prop.add("sequence", practicePlan.getSequence());
            prop.add("practiceType", practicePlan.getType());
            return prop;
        });
        // 根据完成的 type 类型来进行解锁
        PracticePlan targetPracticePlan;
        switch (type) {
            // 如果完成的是小课介绍，需要将小目标解锁
            case PracticePlan.INTRODUCTION:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.CHALLENGE)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是小目标
            case PracticePlan.CHALLENGE:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.KNOWLEDGE && plan.getSeries() == 1)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是知识点
            case PracticePlan.KNOWLEDGE:
            case PracticePlan.KNOWLEDGE_REVIEW:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> (plan.getType() == PracticePlan.WARM_UP
                                || plan.getType() == PracticePlan.WARM_UP_REVIEW)
                                && plan.getSeries() == series)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是巩固练习，解锁简单应用题
            case PracticePlan.WARM_UP:
            case PracticePlan.WARM_UP_REVIEW:
                List<PracticePlan> practicePlanList = practicePlans.stream()
                        .filter(plan -> (plan.getType() == PracticePlan.APPLICATION_BASE
                                || plan.getType() == PracticePlan.APPLICATION_UPGRADED)
                                && plan.getSeries() == series).collect(Collectors.toList());

                practicePlanList.forEach(practicePlan1 -> practicePlanDao.unlock(practicePlan1.getId()));

                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> (plan.getType() == PracticePlan.KNOWLEDGE
                                || plan.getType() == PracticePlan.KNOWLEDGE_REVIEW)
                                && plan.getSeries() == series + 1)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果是第一道应用题,则解锁下一道应用题和下一节的知识点
            case PracticePlan.APPLICATION_BASE:
            case PracticePlan.APPLICATION_UPGRADED:
//                targetPracticePlan = practicePlans.stream()
//                        .filter(plan -> (plan.getType() == PracticePlan.APPLICATION_BASE
//                                || plan.getType() == PracticePlan.APPLICATION_UPGRADED)
//                                && plan.getSeries() == series && plan.getSequence() == sequence + 1)
//                        .findAny().orElse(null);
//                if (targetPracticePlan != null) {
//                    practicePlanDao.unlock(targetPracticePlan.getId());
//                    targetPracticePlan = practicePlans.stream()
//                            .filter(plan -> (plan.getType() == PracticePlan.KNOWLEDGE
//                                    || plan.getType() == PracticePlan.KNOWLEDGE_REVIEW)
//                                    && plan.getSeries() == series + 1)
//                            .findAny().orElse(null);
//                    if (targetPracticePlan != null) {
//                        practicePlanDao.unlock(targetPracticePlan.getId());
//                    }
//                }
                break;
            default:
                break;
        }
    }

    @Override
    public int calculateSectionStatus(List<PracticePlan> practicePlans, Integer series) {
        boolean unlocked = practicePlans.stream()
                .filter(plan -> series.equals(plan.getSeries()))
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 || lock2).orElse(false);
        boolean complete = practicePlans.stream()
                .filter(plan -> series.equals(plan.getSeries()))
                .filter(plan -> (plan.getType() != PracticePlan.APPLICATION_BASE &&
                        plan.getType() != PracticePlan.APPLICATION_UPGRADED))
                .map(PracticePlan::getStatus)
                .reduce((status1, status2) -> status1 * status2).orElse(0).equals(1);

        if (!unlocked) {
            return -1;
        } else {
            if (complete) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    // 用来做延伸学习、学习报告的解锁状态控制
    @Override
    public boolean calculateProblemUnlocked(List<PracticePlan> practicePlans) {
        return practicePlans.stream()
                .filter(plan -> (plan.getType() != PracticePlan.APPLICATION_BASE &&
                        plan.getType() != PracticePlan.APPLICATION_UPGRADED))
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 && lock2)
                .orElse(false);
    }


}
