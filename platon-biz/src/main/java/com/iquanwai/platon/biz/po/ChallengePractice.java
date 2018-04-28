package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("小目标")
public class ChallengePractice {
    private int id;
    @ApiModelProperty("问题id")
    private Integer problemId;
    @ApiModelProperty("提交内容")
    private String content;
    @ApiModelProperty("提交id")
    private Integer submitId;
    @ApiModelProperty("提交时间")
    private String submitUpdateTime;
    @ApiModelProperty("计划id")
    private Integer planId;

    public ChallengePractice(Integer problemId) {
        this.id = problemId;
        this.problemId = problemId;
    }
}
