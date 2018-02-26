package com.iquanwai.platon.biz.domain.fragmentation.plan;

import lombok.Data;

/**
 * 学习计划各小节各题型完成情况
 */
@Data
public class PlanSeriesStatus {

    private Integer practicePlanId;
    private Integer planId; // 计划 Id
    private String practiceId; // 训练 Id 集合
    private Integer series; // 小节号
    private Integer sequence; // 顺序号
    private Integer type; // 小题类型
    private Boolean unlock; // 是否锁定
    private Boolean complete; // 是否完成

}
