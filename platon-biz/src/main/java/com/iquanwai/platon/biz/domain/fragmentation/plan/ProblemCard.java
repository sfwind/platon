package com.iquanwai.platon.biz.domain.fragmentation.plan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@ApiModel("课程卡片信息")
@Data
public class ProblemCard {

    @ApiModelProperty("课程 id")
    private Integer planId;
    @ApiModelProperty("课程 id")
    private Integer problemId;
    @ApiModelProperty("课程全称")
    private String name;
    @ApiModelProperty("课程缩写")
    private String abbreviation;
    @ApiModelProperty("卡片张数")
    private Integer completeCount;

}
