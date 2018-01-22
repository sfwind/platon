package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 */
@Data
public class ApplyQuestionGroupDto {
    private Boolean payApplyFlag;
    private List<ApplyQuestionDto> questions;
}
