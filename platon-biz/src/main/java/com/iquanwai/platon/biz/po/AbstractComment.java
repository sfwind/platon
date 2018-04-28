package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/5/3.
 */
@Data
public abstract class AbstractComment {
    private int id;
    @ApiModelProperty("被回复的讨论id")
    private Integer repliedId;    
    @ApiModelProperty("讨论内容")
    private String comment;    
    @ApiModelProperty("用户id")
    private Integer profileId; 
    @ApiModelProperty("回复时间")
    private Date addTime;    
    @ApiModelProperty("排序优先级")
    private Integer priority; 
    @ApiModelProperty("被回复人id")
    private Integer repliedProfileId;    
    @ApiModelProperty("回复的讨论")
    private String repliedComment; 
    @ApiModelProperty("是否删除(0-未删除，1-已删除)")
    private Integer del; 
    @ApiModelProperty("被回复的评论是否删除(0-未删除，1-已删除)")
    private Integer repliedDel; 
    @ApiModelProperty("回复讨论的发表人名字")
    private String repliedName;
    @ApiModelProperty("回复人名字")
    private String name;
    @ApiModelProperty("回复人头像")
    private String avatar;
    @ApiModelProperty("回复时间标准化格式")
    private String discussTime;
    @ApiModelProperty("回复人角色")
    private Integer role;
    @ApiModelProperty("回复人签名")
    private String signature;
    @ApiModelProperty("是否是本人的回复")
    private Boolean isMine;
    @ApiModelProperty("引用id")
    private Integer referenceId;
}
