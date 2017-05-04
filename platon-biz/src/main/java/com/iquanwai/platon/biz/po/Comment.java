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

}
