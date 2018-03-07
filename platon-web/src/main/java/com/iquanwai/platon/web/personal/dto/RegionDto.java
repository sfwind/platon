package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/6.
 */
@Data
@ApiModel("区域信息")
public class RegionDto {
    @ApiModelProperty("省列表")
    private List<AreaDto> provinceList;
    @ApiModelProperty("城市列表")
    private List<AreaDto> cityList;

}
