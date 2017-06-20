package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumComment {
    private int id;
    private Integer answerId; //回答id
    private Integer commentProfileId; //评论者id
    private String comment; //回复
    private Boolean del; //是否删除（0-未删除，1-已删除）
    private Integer repliedId; //回复评论id
    private Integer repliedProfileId; //被评论者id
    private Boolean repliedDel; //被评论是否删除（0-有效，1-无效）
    private Date addTime; //添加时间

    /** 非DB字段，业务字段 **/
    private String publishTimeStr;
    private String authorUserName; // 作者名字
    private String authorHeadPic; // 作者头像

    private String repliedComment; // 被回复的评论内容
    private String repliedName; // 被回复人的名字
}
