package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
@ApiModel("选项")
public class Choice {
    private int id;
    @ApiModelProperty("问题id")
    private Integer questionId; 
    @ApiModelProperty("题干")
    private String subject; 
    @ApiModelProperty("选项序号")
    private Integer sequence; 
    @ApiModelProperty("是否正确（1-是，0-否）")
    private Boolean isRight; 
    @ApiModelProperty("用户是否选择")
    private Boolean selected;  
    @ApiModelProperty("是否删除")
    private Integer del;
}
