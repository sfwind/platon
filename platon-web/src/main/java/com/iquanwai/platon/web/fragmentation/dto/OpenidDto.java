package com.iquanwai.platon.web.fragmentation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2018/4/3.
 */
@ApiModel("openid model")
@Data
public class OpenidDto {
    @ApiModelProperty("openid")
    private String openid;
}
