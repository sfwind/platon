package com.iquanwai.platon.biz.po.apply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-22
 * <p>
 * 商学院申请的选项
 */
@Data
@ApiModel("商学院申请选项")
public class BusinessApplyChoice {
    private Integer id;
    @ApiModelProperty("题干")
    private String subject;
    @ApiModelProperty("问题id")
    private Integer questionId;
    @ApiModelProperty("顺序")
    private Integer sequence;
    @ApiModelProperty("默认选中")
    private Boolean defaultSelected;

    private Boolean del;
}
