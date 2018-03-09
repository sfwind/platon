package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
@ApiModel("区域信息")
@NoArgsConstructor
@AllArgsConstructor
public class AreaDto {
    @ApiModelProperty("区域id")
    private String id;
    @ApiModelProperty("名称")
    private String value;
    @ApiModelProperty("父区域id")
    private String parentId;
}
