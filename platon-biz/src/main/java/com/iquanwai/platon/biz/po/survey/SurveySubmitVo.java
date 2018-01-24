package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

import java.util.List;

@Data
public class SurveySubmitVo {
    private String questionCode;
    private String userValue;
    private Integer choiceId;
    private List<String> choiceIds;
}
