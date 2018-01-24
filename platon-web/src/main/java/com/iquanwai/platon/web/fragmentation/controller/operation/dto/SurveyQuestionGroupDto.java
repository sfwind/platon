package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * 题目组
 */
@Data
public class SurveyQuestionGroupDto {
    private List<SurveyQuestionDto> surveyQuestions;
    private Boolean subscribe;
    private String subscribeQrCode;
}
