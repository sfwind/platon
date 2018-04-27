package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

@Data
public class SurveyDefine {
    private Integer id;
    private Integer defineId;
    private String name;
    private Integer type;

    public static final Integer CATEGORY = 1;
    public static final Integer VARIABLE = 2;
}
