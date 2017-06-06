package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by xfduan on 2017/6/6.
 */
@Data
public class ApplicationSubmitDraft {

    private int id;
    private String openid; //提交用户openid
    private Integer profileId; // 用户 id
    private Integer applicationId; //应用练习id
    private Integer planId; //提升计划id
    private String content; //提交内容
    private Date updateTime; //最后更新时间
    private Date publishTime; // 第一次提交时间
    private Integer length; //字数

}
