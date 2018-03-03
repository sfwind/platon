package com.iquanwai.platon.web.personal.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
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
