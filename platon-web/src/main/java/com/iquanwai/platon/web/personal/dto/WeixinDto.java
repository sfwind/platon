package com.iquanwai.platon.web.personal.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
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
