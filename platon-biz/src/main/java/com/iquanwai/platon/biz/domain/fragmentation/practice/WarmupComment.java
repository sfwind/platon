package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/8/9.
 */
@Data
public class WarmupComment {
    private int id;
    private String comment;    //讨论内容
    private Integer priority; //排序优先级,1-优质回答,0-普通
    private Integer del; //是否删除(0-未删除，1-已删除)

    private String name;  //回复人名字
    private String avatar;//回复人头像
    private String discussTime;//回复时间标准化格式
    private Integer role; //回复人角色
    private String signature; //回复人签名
    private Boolean isMine; //是否是本人的回复
    private Integer warmupPracticeId; //巩固练习id
    private Integer originDiscussId; //讨论最早发起的评论id
    private Date addTime; //回复时间
    private Integer priorityComment; //排序优先级,1-包含优质回答讨论或就是优质回答,0-普通

    private List<WarmupPracticeDiscuss> warmupPracticeDiscussList = Lists.newArrayList(); //基于这条评论的讨论


    @ApiModelProperty("选择题主要评论")
    private WarmupPracticeDiscuss originDiscuss;
    @ApiModelProperty("选择题次要评论")
    private WarmupPracticeDiscuss priorityDiscuss;

}
