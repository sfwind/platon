package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class  ApplicationPractice {
    private int id;
    private String topic; //任务标题
    private String description; // 题干
    private Integer knowledgeId; //知识点id
    @Deprecated
    private Integer sceneId; //子场景id
    private Integer difficulty; //难易度（1-容易，2-普通，3-困难）
    private Integer problemId; //课程id
    private Integer sequence; //出现顺序
    private Boolean del; //是否删除(0-否,1-是)
    private String practiceUid; //任务唯一编号
    private String pic; //题目图片
    private Integer type; //题目类型(1-应用题,2-案例题)

    private String name; //应用题名字，非db字段
    private String content; //提交内容，非db字段
    private Integer submitId; // 提交id，非db字段
    private String submitUpdateTime;// 非db字段
    private Integer voteCount; // 点赞数 非db字段
    private Integer commentCount; // 评论数 非db字段
    private Integer applicationScore; // 应用题得分
    private Integer voteStatus; // 点赞状态，是否可以点赞 非db字段
    private Integer planId; // 计划id 非db字段
    private Integer requestCommentCount; //求点赞 非db字段
    private Boolean request; //是否已经求点评 非db字段
    private Boolean feedback; // 是否已经被教练点评，非db字段

    private String draft; // 草稿内容
    private Integer draftId; // 草稿 id
    private Boolean overrideLocalStorage; // 是否覆盖本地 localStorage
    private Boolean isBaseApplication; // 是否是基础应用题
    private Boolean isLastApplication; // 是否是最后一题

    /**
     * 草稿内容和提交内容是否相等
     */
    private Boolean isSynchronized;

}
