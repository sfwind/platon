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
                    prop.add("applicationId", Integer.valueOf(practicePlan.getPracticeId()));
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
    public void completePracticePlan(Integer profileId, PracticePlan practicePlan) {
        if (practicePlan == null) {
            return;
        }

        int practicePlanId = practicePlan.getId();
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
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
                // 解锁第一小节的第一个练习
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getSeries() == 1)
                        .min((plan1, plan2) -> plan1.getSequence() - plan2.getSequence())
                        .orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 完成知识点或者课前思考,解锁下一个练习
            case PracticePlan.KNOWLEDGE:
            case PracticePlan.KNOWLEDGE_REVIEW:
            case PracticePlan.PREVIEW:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getSeries() == series && plan.getSequence() > practicePlan.getSequence())
                        .min((plan1, plan2) -> plan1.getSequence() - plan2.getSequence())
                        .orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是选择题，解锁所有应用题以及下一节的第一个练习
            case PracticePlan.WARM_UP:
            case PracticePlan.WARM_UP_REVIEW:
                List<PracticePlan> practicePlanList = practicePlans.stream()
                        .filter(plan -> (PracticePlan.isApplicationPractice(plan.getType()))
                                && plan.getSeries() == series).collect(Collectors.toList());

                practicePlanList.forEach(practicePlan1 -> practicePlanDao.unlock(practicePlan1.getId()));

                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getSeries() == series + 1)
                        .min((plan1, plan2) -> plan1.getSequence() - plan2.getSequence())
                        .orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果是第一道应用题,则解锁下一道应用题和下一节的知识点
            case PracticePlan.APPLICATION_BASE:
            case PracticePlan.APPLICATION_UPGRADED:
            case PracticePlan.APPLICATION_GROUP:
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
                .filter(plan -> !PracticePlan.isApplicationPractice(plan.getType()))
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
                .filter(plan -> !PracticePlan.isApplicationPractice(plan.getType()))
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 && lock2)
                .orElse(false);
    }


}
