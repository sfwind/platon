package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel("圈外课")
public class ProblemsFlow {

    private Integer id;
    @ApiModelProperty("课程缩略名")
    private String abbreviation;
    @ApiModelProperty("课程名")
    private String name;
    @ApiModelProperty("是否热门")
    private Boolean hot;
    @ApiModelProperty("缩略图")
    private String thumbnail;

}
