package com.iquanwai.platon.biz.po.survey.report;

import lombok.Data;

import java.util.List;

@Data
public class SurveyCategoryInfo {
    private Integer categoryId;
    private String legend;
    private List<SurveyVariableInfo> detail;
}
