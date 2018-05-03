package com.iquanwai.platon.biz.po.survey.report;

import lombok.Data;

@Data
public class SurveyVariableInfo {
    private Integer variableId;
    private String category;
    private Double value;
    private Integer max;

    private String suggest;
}

