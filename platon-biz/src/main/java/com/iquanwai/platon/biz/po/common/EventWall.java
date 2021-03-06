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
    private String subHead; // 副标题
    private String time;
    private Boolean banner; // 是否放在banner
    private Integer type; // 活动类型
    private String pic; // 头图
    private String destUrl; // 千聊链接
    private Date startTime; // 开始时间
    private Date addTime;
    private Date updateTime;
    private Boolean del;
    private Boolean showTime; //是否显示时间
    private Integer visibility; // 会员可见性
    private Integer problemId;
    private Integer visibleProblemId; // 针对课程用户的可见性


    public static final int LIVE = 1;// 直播
    public static final int WORK_ANALYSIS = 2;// 作业分析
    public static final int OFFLINE = 3; // 线下活动
    public static final int MORE = 4; // 更多精彩

    public interface VisibleLevel {
        int NO_RESTRICT = 0; //不作限制
        int NO_PAY_NO_PLAN = 1; //非付费
        int NOT_ELITE = 2; // 非精英
        int ELITE = 3; // 精英版
        int CAMP = 4; // 专项课
        int RISE_MEMBER = 5; // 付费用户
    }
}
