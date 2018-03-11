package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.Problem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
@ApiModel("课程信息")
public class PlanDto {
    @ApiModelProperty("课程名称")
    private String name;
    @ApiModelProperty("课程分数")
    private Integer point;
    @ApiModelProperty("课程id")
    private Integer problemId;
    @ApiModelProperty("课程计划id")
    private Integer planId;
    @ApiModelProperty("课程头图地址")
    private String pic;
    @ApiModelProperty("已完成的节数")
    private Integer completeSeries;
    @ApiModelProperty("总节数")
    private Integer totalSeries;
    @ApiModelProperty("离截止日期天数")
    private Integer deadline;
    @ApiModelProperty("开始日期")
    private Date startDate;
    @ApiModelProperty("关闭时间")
    private Date closeTime;
    @ApiModelProperty("是否可以学习")
    private Boolean learnable;
    @ApiModelProperty("该训练计划对应的课程")
    private Problem problem;
    @ApiModelProperty("该训练计划对应的课程")
    private String errMsg;

}
