package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

/**
 * @author nethunder
 * 问卷选项
 */
@Data
public class SurveyChoice {
    private Integer id;
    /**
     * 题目分类
     */
    private String category;
    /**
     * 题目code
     */
    private String questionCode;
    /**
     * 选项题干
     */
    private String subject;
    /**
     * 选项序号
     */
    private Integer sequence;
    /**
     * 是否默认选中
     */
    private Boolean defaultSelected;
    /**
     * 是否删除
     */
    private Boolean del;
}
