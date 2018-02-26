package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.iquanwai.platon.biz.po.PracticePlan;

import java.util.List;

public interface PracticePlanStatusManager {
    /** 根据 PlanId 完成 PracticePlan */
    void completePracticePlan(Integer profileId, Integer practicePlanId);

    /**
     * 查看小节的状态
     * @return -1 锁定
     *  return 0 未完成
     *  return 1 已完成
     * */
    int calculateSectionStatus(List<PracticePlan> practicePlans, Integer series);

    /** 用来做延伸学习、学习报告的解锁状态控制 */
    boolean calculateProblemUnlocked(List<PracticePlan> practicePlans);
}
