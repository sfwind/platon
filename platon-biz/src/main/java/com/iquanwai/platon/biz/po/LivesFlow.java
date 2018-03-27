package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("直播对象")
public class LivesFlow extends FlowData {

    private Integer id;
    @ApiModelProperty("直播名称")
    private String name;
    @ApiModelProperty("主讲人")
    private String speaker;
    @ApiModelProperty("主讲人描述")
    private String speakerDesc;
    @ApiModelProperty("缩略图")
    private String thumbnail;
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("链接地址")
    private String linkUrl;

    @ApiModelProperty("开始时间 str")
    private String startTimeStr;

}
