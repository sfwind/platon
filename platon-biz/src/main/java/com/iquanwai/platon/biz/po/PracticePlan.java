package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class PracticePlan {
    private int id;
    private Integer planId; //训练id
    private Integer type; //题目类型（0-课程介绍 1-巩固练习，2-巩固练习，11-应用练习，12-应用练习，21-小目标，31-知识理解，32-知识回顾）
    private String practiceId; //练习id,多个时用逗号隔开
    private Boolean unlocked; // 是否解锁
    private Integer series; // 节号
    private Integer sequence; //节内顺序
    private Integer knowledgeId; //知识点id
    private Integer status; //题目状态（0-未完成，1-已完成）
    @Deprecated
    private Boolean summary; //是否已总结(0-否，1-是) 废弃


    public static final int INTRODUCTION = 20; // 课程介绍
    public static final int CHALLENGE = 21; // 小目标

    public static final int KNOWLEDGE = 31; // 知识理解
    public static final int KNOWLEDGE_REVIEW = 32; // 知识回顾
    public static final int WARM_UP = 1; // 选择题
    public static final int WARM_UP_REVIEW = 2; // 综合选择题
    public static final int APPLICATION = 11; // 应用题
    public static final int APPLICATION_REVIEW = 12; // 综合应用题

    public static final int APPLICATION_BASE = 11; // 简单应用题
    public static final int APPLICATION_UPGRADED = 12; // 困难应用题

    public interface STATUS {
        /**
         * 未完成
         */
        int UNCOMPLETED = 0;
        /**
         * 已完成
         */
        int COMPLETED = 1;
        /**
         * 永不解锁
         */
        int NEVER_UNLOCK = 2;
    }
}
