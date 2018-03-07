package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2017/12/6.
 */
@Data
@ApiModel("微信id")
public class WeixinDto {
    @ApiModelProperty("微信id")
    private String weixinId;
}
