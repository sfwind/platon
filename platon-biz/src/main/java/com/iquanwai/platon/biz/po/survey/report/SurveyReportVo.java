package com.iquanwai.platon.biz.po.survey.report;

import lombok.Data;

import java.util.List;

@Data
public class SurveyReportVo {
    private String surveyName;
    private List<SurveyVariableInfo> variables;
}
