package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * 问卷提交
 */
@Data
public class SurveyResult {
    private Integer id;
    /**
     * 题目分类
     */
    private String category;
    /**
     * 依赖的问卷id
     */
    private Integer referSurveyId;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 层数
     */
    private Integer level;
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
    /**
     * 是否生成过报告
     */
    private Boolean generatedReport;
    /**
     * 是否可以用来生成报告
     */
    private Boolean reportValid;
}
