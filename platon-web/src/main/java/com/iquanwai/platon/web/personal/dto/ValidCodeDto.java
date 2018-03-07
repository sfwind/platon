package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 17/6/28.
 */
@Data
@ApiModel("验证码信息")
public class ValidCodeDto {
    @ApiModelProperty("验证码")
    private String code;
    @ApiModelProperty("区号")
    private String areaCode;
    @ApiModelProperty("电话")
    private String phone;
}
