package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/15.
 */
@Data
public class ApplicationSubmit {
    private int id;
    private Integer profileId; //用户id
    private Integer applicationId; //应用练习id
    private Integer planId; //提升计划id
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
    private Integer priority; // 排序优先级
    private Date updateTime; //最后更新时间
    private Date publishTime; // 第一次提交时间
    private Date lastModifiedTime; //最近一次内容提交时间
    private Boolean requestFeedback; //是否求点评
    private Boolean feedback; // 教练是否已点评
    private Integer length; //字数
    private Integer problemId;//课程id
    private Date requestTime;//求点评时间
    private Date feedBackTime;//首次点评时间

    private Integer voteCount; //非db字段 点赞数
    private boolean voteStatus; //非db字段 是否点赞
    private String topic; //非db字段 应用练习标题
}
