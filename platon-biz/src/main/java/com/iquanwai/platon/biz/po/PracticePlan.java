package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class PracticePlan {
    private int id;
    private Integer planId; //碎片化训练id
    private Integer type; //题目类型（1-单选题，2-多选题，11-应用题，21-挑战题）
    private Integer practiceId; //练习id
    private Boolean lock; // 是否解锁
    private Integer status; //题目状态（0-未完成，1-已完成）
}
