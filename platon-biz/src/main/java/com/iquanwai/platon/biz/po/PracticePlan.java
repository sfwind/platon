package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class PracticePlan {
    private int id;
    private Integer planId; //碎片化训练id
    private Integer type; //题目类型（1-理解训练，11-应用训练，21-小目标）
    private String practiceId; //练习id,多个时用逗号隔开
    private Boolean unlocked; // 是否解锁
    private Integer series; // 节号
    private Integer sequence; //节内顺序
    @Deprecated
    private Integer knowledgeId; //知识点id
    private Integer status; //题目状态（0-未完成，1-已完成）
    @Deprecated
    private Boolean summary; //是否已总结(0-否，1-是) 废弃

    public static final int WARM_UP = 1;
    public static final int WARM_UP_REVIEW = 2;
    public static final int APPLICATION = 11;
    public static final int APPLICATION_REVIEW = 12;
    public static final int CHALLENGE = 21;
    public static final int KNOWLEDGE = 31;
    public static final int KNOWLEDGE_REVIEW = 32;
}
