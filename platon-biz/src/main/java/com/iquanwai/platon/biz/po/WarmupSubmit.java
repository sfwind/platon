package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class WarmupSubmit {
    private int id;
    private String openid; //提交人openid
    private Integer questionId; //热身训练id
    private Integer planId; //提升计划id
    private String content; //提交答案（多个时逗号隔开）
    private Boolean isRight; //是否完全正确（1-是，0-否）
    private Integer score; //得分

}
