package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
public class Comment {
    private Integer id;
    private Integer type;  //评论类型 1-普通评论 2-助教 3-大咖 4-圈圈
    private Integer moduleId; //评论的模块 1-挑战任务 2-应用任务
    private Integer referencedId; //外键
    private String commentOpenId;  //评论人
    private String content; //评论内容
    private Integer del; //是否删除
    private Integer device; // 提交设备
    private Date AddTime; //添加时间

    private Integer priority; // 排序优先级 官方-1，普通-0 或者其他优先级，例如是否精彩评论
    private Integer repliedId; // 被回复的评论id,为null是则不是回复
    private String repliedOpenid;// 被回复人的openid,repliedId不为null时有值
    private String repliedComment; // 被回复的评论内容
}
