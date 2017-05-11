package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/5/2.
 */
@Data
public class EventWall {
    private Integer id;
    private String title; // 活动标题
    private String publisher; // 活动发起人
    private String pic; // 头图
    private String destUrl; // 千聊链接
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
    private Date addTime;
    private Date updateTime;
    private Boolean del;

    private String startStr;
    private String endStr;
}
