package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-22
 */
@Data
public class ApplyQuestionDto {
    private Integer series;
    private List<BusinessApplyQuestion> questions;
}
