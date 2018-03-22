package com.iquanwai.platon.biz.po.apply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 商学院申请的提交记录
 */
@Data
@ApiModel("商学院申请的提交记录")
public class BusinessApplySubmitVO {
    @ApiModelProperty("问题编号")
    private Integer questionId;
    @ApiModelProperty("用户回答的内容")
    private String userValue;
    @ApiModelProperty("用户选择的编号")
    private Integer choiceId;
}
