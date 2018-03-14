package com.iquanwai.platon.web.fragmentation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/9/4.
 */
@Data
@ApiModel("作业提交")
public class SubmitDto {
    @ApiModelProperty("提交答案")
    private String answer;
    @ApiModelProperty("提交草稿")
    private String draft;
}
