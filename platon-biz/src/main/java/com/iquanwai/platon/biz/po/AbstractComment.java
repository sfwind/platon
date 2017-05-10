package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/5/3.
 */
@Data
public abstract class AbstractComment {
    private int id;
    private Integer repliedId;    //被回复的讨论id
    private String comment;    //讨论内容
    private String openid;    //回复人
    private Date addTime;    //回复时间
    private Integer priority; //排序优先级,1-大咖,圈外工作人员,助教,0-普通人
    private String repliedOpenid;    //被回复人
    private String repliedComment; //回复的讨论
    private Integer del; //是否删除(0-未删除，1-已删除)
    private Integer repliedDel; //被回复的评论是否删除(0-未删除，1-已删除)

    private String repliedName; //回复讨论的发表人名字 非db字段
    private String name;  //回复人名字 非db字段
    private String avatar;//回复人头像 非db字段
    private String discussTime;//回复时间标准化格式 非db字段
    private Integer role; //回复人角色 非db字段
    private String signature; //回复人签名 非db字段
    private Boolean isMine; //是否是本人的回复 非db字段

    private Integer referenceId; // 引用id 是db字段的映射，减少代码量

}
