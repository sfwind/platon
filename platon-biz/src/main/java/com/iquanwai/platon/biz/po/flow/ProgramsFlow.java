package com.iquanwai.platon.biz.po.flow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2018/4/25.
 */
@Data
@ApiModel("圈外项目")
public class ProgramsFlow {

    private Integer id;
    @ApiModelProperty("项目id,3-核心能力,5-专项课,8-商业思维")
    private Integer memberTypeId;
    @ApiModelProperty("底图链接")
    private String pic;
    @ApiModelProperty("剩余人数")
    private Integer remainNumber;
    @ApiModelProperty("实际售价")
    private Double price;
    @ApiModelProperty("初始售价")
    private Double initPrice;
    @ApiModelProperty("几月入学")
    private Integer month;
    @ApiModelProperty("是否售卖")
    private boolean open;
    @ApiModelProperty("项目状态,1-预约,2-打折售卖,3-正常售卖,4-剩余名额")
    private Integer type;
    @ApiModelProperty("跳转地址")
    private String url;
}
