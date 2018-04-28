package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/8/9.
 */
@Data
@ApiModel("选择题评论")
public class WarmupComment {
    private int id;
    @ApiModelProperty("讨论内容")
    private String comment;    
    @ApiModelProperty("排序优先级")
    private Integer priority; 
    @ApiModelProperty("是否删除(0-未删除，1-已删除)")
    private Integer del; 
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
    @ApiModelProperty("巩固练习id")
    private Integer warmupPracticeId; 
    @ApiModelProperty("讨论最早发起的评论id")
    private Integer originDiscussId; 
    @ApiModelProperty("回复时间")
    private Date addTime; 
    @ApiModelProperty("排序优先级")
    private Integer priorityComment; 
    @ApiModelProperty("基于这条评论的讨论")
    private List<WarmupPracticeDiscuss> warmupPracticeDiscussList = Lists.newArrayList(); 
    @ApiModelProperty("选择题主要评论")
    private WarmupPracticeDiscuss originDiscuss;
    @ApiModelProperty("选择题次要评论")
    private WarmupPracticeDiscuss priorityDiscuss;

}
