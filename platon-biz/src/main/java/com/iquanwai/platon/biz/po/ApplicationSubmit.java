package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/15.
 */
@Data
@ApiModel("应用题提交")
public class ApplicationSubmit {
    private int id;
    @ApiModelProperty("用户id")
    private Integer profileId; 
    @ApiModelProperty("应用练习id")
    private Integer applicationId; 
    @ApiModelProperty("提升计划id")
    private Integer planId; 
    @ApiModelProperty("提交内容")
    private String content; 
    @ApiModelProperty("是否已加分（0-否，1-是）")
    private Integer pointStatus;
    @ApiModelProperty("排序优先级")
    private Integer priority;  
    @ApiModelProperty("最后更新时间")
    private Date updateTime; 
    @ApiModelProperty("第一次提交时间")
    private Date publishTime;  
    @ApiModelProperty("最近一次内容提交时间")
    private Date lastModifiedTime; 
    @ApiModelProperty("是否求点评")
    private Boolean requestFeedback; 
    @ApiModelProperty("教练是否已点评")
    private Boolean feedback;  
    @ApiModelProperty("字数")
    private Integer length; 
    @ApiModelProperty("课程id")
    private Integer problemId;
    @ApiModelProperty("求点评时间")
    private Date requestTime;
    @ApiModelProperty("首次点评时间")
    private Date feedBackTime;
    @ApiModelProperty("提交内容中是否含有图片")
    private Boolean hasImage; 
    @ApiModelProperty("点赞数")
    private Integer voteCount; 
    @ApiModelProperty("是否点赞")
    private boolean voteStatus;  
    @ApiModelProperty("应用练习标题")
    private String topic; 
}
