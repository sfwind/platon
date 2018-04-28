package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("练习model")
public class PracticePlan {
    private int id;
    @ApiModelProperty("训练id")
    private Integer planId; //
    @ApiModelProperty("训练id")
    private Integer type; //题目类型（0-课程介绍 1-选择题，2-选择题，11-应用题，12-应用题，13-案例题, 21-小目标，31-知识理解，32-知识回顾）
    @ApiModelProperty("训练id")
    private String practiceId; //练习id,多个时用逗号隔开
    @ApiModelProperty("训练id")
    private Boolean unlocked; // 是否解锁
    @ApiModelProperty("训练id")
    private Integer series; // 节号
    @ApiModelProperty("训练id")
    private Integer sequence; //节内顺序
    @ApiModelProperty("训练id")
    private Integer knowledgeId; //知识点id
    @ApiModelProperty("题目状态（0-未完成，1-已完成）")
    private Integer status; //


    public static final int INTRODUCTION = 20; // 课程介绍
    public static final int CHALLENGE = 21; // 小目标

    public static final int KNOWLEDGE = 31; // 知识理解
    public static final int KNOWLEDGE_REVIEW = 32; // 知识回顾
    public static final int WARM_UP = 1; // 选择题
    public static final int WARM_UP_REVIEW = 2; // 综合选择题
    public static final int APPLICATION_BASE = 11; // 简单应用题
    public static final int APPLICATION_UPGRADED = 12; // 困难应用题
    public static final int APPLICATION_GROUP = 13; // 小组案例题
    public static final int PREVIEW = 41; // 课前思考

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

    public static boolean isWarmupPractice(Integer type){
        return type == WARM_UP || type == WARM_UP_REVIEW;
    }

    public static boolean isApplicationPractice(Integer type){
        return type == APPLICATION_BASE || type == APPLICATION_UPGRADED;
    }
    public static boolean isGroupWork(Integer type){
        return  type == APPLICATION_GROUP;
    }

    public static boolean isKnowledge(Integer type){
        return type == KNOWLEDGE || type == KNOWLEDGE_REVIEW;
    }

    public static boolean isPreview(Integer type){
        return type == PREVIEW;
    }

    public static String getPracticePlanTitle(Integer type) {
        if (isPreview(type)) {
            return "课前思考";
        } else if (isKnowledge(type)) {
            return "知识学习";
        } else if (isWarmupPractice(type)) {
            return "知识测验";
        } else if (isApplicationPractice(type)) {
            return "实战演练";
        }else if(isGroupWork(type)){
            return "小组作业";
        }
        //默认
        return "课程练习";
    }
}
