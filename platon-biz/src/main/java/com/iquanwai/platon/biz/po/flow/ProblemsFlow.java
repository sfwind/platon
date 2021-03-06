package com.iquanwai.platon.biz.po.flow;

import com.iquanwai.platon.biz.po.FlowData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel("圈外课")
public class ProblemsFlow extends FlowData {

    private Integer id;
    @ApiModelProperty("ProblemId")
    private Integer problemId;
    @ApiModelProperty("课程缩略名")
    private String abbreviation;
    @ApiModelProperty("课程名")
    private String name;
    @ApiModelProperty("是否热门")
    private Boolean hot;
    @ApiModelProperty("缩略图")
    private String thumbnail;

}
