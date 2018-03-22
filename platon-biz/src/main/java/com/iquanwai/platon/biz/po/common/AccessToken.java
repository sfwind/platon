package com.iquanwai.platon.biz.po.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class AccessToken {
    @ApiModelProperty("accessToken")
    private String accessToken;
}
