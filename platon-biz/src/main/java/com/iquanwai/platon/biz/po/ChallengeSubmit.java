package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/15.
 */
@Data
public class ChallengeSubmit {
    private int id;
    private String openid; //提交用户openid
    private Integer challengeId; //小目标id
    private Integer planId; //提升计划id
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
    private Date updateTime; //最后更新时间
    public Date publishTime; // 第一次提交时间
    private Integer length; //字数
}
