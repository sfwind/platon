package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

@Data
public class SurveyReportSuggest {
    private Integer id;
    private Integer categoryId;
    private Integer variableId;
    private String suggest;
    private String pointRange;
}
