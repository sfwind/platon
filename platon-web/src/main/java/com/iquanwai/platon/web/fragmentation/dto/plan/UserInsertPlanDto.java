package com.iquanwai.platon.web.fragmentation.dto.plan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("用户计划dto")
public class UserInsertPlanDto {

    @ApiModelProperty("用户id")
    private List<Integer> profileIds;
    @ApiModelProperty("开始增加节数")
    private Integer startSeries;
    @ApiModelProperty("结束增加节数")
    private Integer endSeries;
    @ApiModelProperty("课程id")
    private Integer problemId;
}
