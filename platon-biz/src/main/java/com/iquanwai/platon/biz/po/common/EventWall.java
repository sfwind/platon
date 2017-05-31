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
    private String password; // 密码
    private Boolean banner; // 是否放在banner
    private Integer type; // 活动类型
    private String pic; // 头图
    private String destUrl; // 千聊链接
    private String memberDestUrl; // 会员跳转的url
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
    private Date addTime;
    private Date updateTime;
    private Boolean del;
    private Boolean showTime; //是否显示时间
    private Integer visibility; // 会员可见性
    private String startStr;
    private String endStr;
}
