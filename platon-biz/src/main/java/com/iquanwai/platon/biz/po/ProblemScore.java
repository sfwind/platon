package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by nethunder on 2017/3/23.
 */
@Data
@ApiModel("课程打分model")
public class ProblemScore {
    private Integer id;
    @ApiModelProperty("用户id")
    private Integer profileId;
    @ApiModelProperty("课程id")
    private Integer problemId;
    @ApiModelProperty("打分问题序号")
    private Integer question;
    @ApiModelProperty("客观题选项的值")
    private Integer choice;
    @ApiModelProperty("主观题答案")
    private String comment;
}
