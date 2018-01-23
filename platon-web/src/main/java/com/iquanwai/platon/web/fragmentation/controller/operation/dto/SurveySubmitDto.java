package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import com.iquanwai.platon.biz.po.survey.SurveySubmitVo;
import lombok.Data;

import java.util.List;

@Data
public class SurveySubmitDto {
    private List<SurveySubmitVo> userSubmits;
}
