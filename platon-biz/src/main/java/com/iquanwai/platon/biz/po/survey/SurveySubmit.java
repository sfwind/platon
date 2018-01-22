package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * 问卷提交
 */
@Data
public class SurveySubmit {
    private Integer id;
    /**
     * 题目分类
     */
    private String category;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 用户id
     */
    private Integer profileId;
    /**
     * openid
     */
    private String openid;
    /**
     * 提交时间
     */
    private Date submitTime;
    /**
     * 是否删除
     */
    private Boolean del;
}
