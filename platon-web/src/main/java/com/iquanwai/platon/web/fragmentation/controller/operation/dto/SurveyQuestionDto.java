package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 */
@Data
public class SurveyQuestionDto {
    private Integer series;
    private List<SurveyQuestion> questions;
}

