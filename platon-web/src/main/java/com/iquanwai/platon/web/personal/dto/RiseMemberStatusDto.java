package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class RiseMemberStatusDto {

    @ApiModelProperty("是否进入倒计时")
    private Boolean goCountDownPage;
    @ApiModelProperty("身份 id")
    private Integer memberTypeId;

}
