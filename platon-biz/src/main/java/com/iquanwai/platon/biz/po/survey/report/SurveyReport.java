package com.iquanwai.platon.biz.po.survey.report;

import lombok.Data;

import java.util.List;

@Data
public class SurveyReport {
    private List<SurveyCategoryInfo> categoryInfos;
    private Integer otherSurveyCount;
    private Boolean showComplete;
}
