package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

/**
 * @author nethunder
 * 问卷题目提交表
 */
@Data
public class SurveyQuestionResult {
    private Integer id;
    private Integer submitId;
    /**
     * 题目分类
     */
    private String category;
    /**
     * 题目code
     */
    private String questionCode;
    /**
     * 选项id
     */
    private Integer choiceId;
    /**
     * 多选id
     */
    private String choiceIds;
    /**
     * 用户提交的文字
     */
    private String userValue;
    /**
     * 是否删除
     */
    private Boolean del;
}
