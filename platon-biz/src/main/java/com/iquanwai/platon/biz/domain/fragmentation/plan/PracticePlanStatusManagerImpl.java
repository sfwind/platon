package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class PracticePlanStatusManagerImpl implements PracticePlanStatusManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    // 根据 PlanId 完成 PracticePlan
    public void completePracticePlan(Integer profileId, Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return;
        }

        // 学习计划 ImprovementPlan 的 id
        int planId = practicePlan.getPlanId();

        // 核实人员信息、并且将 status 改成 1，将该条记录置成完成
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        boolean planAuthCheck = improvementPlans.stream().map(ImprovementPlan::getId).collect(Collectors.toList()).contains(planId);
        if (planAuthCheck) {
            practicePlanDao.complete(practicePlanId);
        }

        int type = practicePlan.getType(); // 当前题目类型
        int series = practicePlan.getSeries(); // 当前小节数

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
            // 如果完成的是小目标，
            case PracticePlan.CHALLENGE:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.KNOWLEDGE && plan.getSeries() == 0)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是知识点
            case PracticePlan.KNOWLEDGE:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.WARM_UP && plan.getSeries() == series)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            // 如果完成的是简单应用题，解锁困难应用题和下一节知识点
            case PracticePlan.APPLICATION_BASE:
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.APPLICATION_UPGRADED && plan.getSeries() == series)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                targetPracticePlan = practicePlans.stream()
                        .filter(plan -> plan.getType() == PracticePlan.KNOWLEDGE && plan.getSeries() == series + 1)
                        .findAny().orElse(null);
                if (targetPracticePlan != null) {
                    practicePlanDao.unlock(targetPracticePlan.getId());
                }
                break;
            default:
                break;
        }
    }

    public boolean calculateSectionUnlocked(Integer planId, Integer series) {
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        return practicePlans.stream()
                .filter(plan -> series.equals(plan.getSeries()))
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 && lock2).orElse(false);
    }

    public boolean calculateSectionStatus(Integer planId, Integer series) {
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        return practicePlans.stream()
                .filter(plan -> series.equals(plan.getSeries()))
                .map(PracticePlan::getStatus)
                .reduce((status1, status2) -> status1 * status2).orElse(0).equals(1);
    }

    // 用来做延伸学习、学习报告的解锁状态控制
    public boolean calculateProblemUnlocked(Integer planId) {
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        return practicePlans.stream()
                .filter(plan -> plan.getType() != PracticePlan.APPLICATION_UPGRADED)
                .map(PracticePlan::getUnlocked)
                .reduce((lock1, lock2) -> lock1 && lock2)
                .orElse(false);
    }



}
