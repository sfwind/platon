package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/2/15.
 */
@Data
public class ChallengeSubmit {
    private int id;
    private String openid; //提交用户openid
    private Integer challengeId; //专题训练id
    private Integer planId; //提升计划id
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
}
