package com.iquanwai.platon.biz.po.apply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 申请提交记录
 */
@Data
@ApiModel("申请提交记录")
public class BusinessApplySubmit {
    private Integer id;
    @ApiModelProperty("申请号")
    private Integer applyId;
    @ApiModelProperty("问题编号")
    private Integer questionId;
    @ApiModelProperty("选择编号")
    private Integer choiceId;
    @ApiModelProperty("选择的内容")
    private String choiceText;
    @ApiModelProperty("填写的内容")
    private String userValue;
}
