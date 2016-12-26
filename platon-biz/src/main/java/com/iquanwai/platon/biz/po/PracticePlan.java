package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class PracticePlan {
    private int id;
    private Integer planId; //碎片化训练id
    private Integer type; //题目类型（1-热身训练，11-应用训练，21-挑战训练）
    private String practiceId; //练习id,多个时用逗号隔开
    private Boolean unlocked; // 是否解锁
    private Integer series; // 组号
    private Integer sequence; //组内顺序
    private Integer knowledgeId; //知识点id
    private Integer status; //题目状态（0-未完成，1-已完成）

    public static final int RADIO = 1;
    public static final int MULTIPLE_CHOICE = 2;
    public static final int APPLICATION = 11;
    public static final int CHALLENGE = 21;
}
