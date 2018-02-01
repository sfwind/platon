package com.iquanwai.platon.biz.domain.fragmentation.plan.manager;

import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
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

    // 根据 PlanId 完成 PracticePlan
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
        boolean planAuthCheck = improvementPlans.stream().map(ImprovementPlan::getId)
                .collect(Collectors.toList()).contains(planId);
        if (planAuthCheck) {
            practicePlanDao.complete(practicePlanId);
        }

        int type = practicePlan.getType(); // 当前题目类型
        int series = practicePlan.getSeries(); // 当前小节数
//        int sequence = practicePlan.getSequence();

        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(practicePlan.getPlanId());

        // 根据完成的 type 类型来进行下一步操作
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
//                .sorted((o1, o2)->o2.getSequence()-o1.getSequence())
                .filter(plan -> series.equals(plan.getSeries()))
                //TODO:如果每节练习数不等于4,此处需修改
                .filter(plan -> plan.getSequence() != 4)
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
                        plan.getType()!= PracticePlan.APPLICATION_UPGRADED))
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 && lock2)
                .orElse(false);
    }


}
