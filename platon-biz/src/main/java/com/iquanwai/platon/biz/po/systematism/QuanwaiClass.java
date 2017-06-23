package com.iquanwai.platon.biz.po.systematism;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/8/29.
 */
@Data
public class QuanwaiClass {
    private int id;
    private Integer classNumber; //按同一门课的班级排序的序号
    private Date openTime; //开课时间
    private Date closeTime; //结业时间
    private Integer courseId; //课程id
    private Integer progress; //进度，当前课程进行到的章节id
    private Integer limit; //班级人数上限
    private Integer season; //课程开班期数
    private Boolean open; //0-关闭报名，1-已开放报名
    private String weixinGroup; //微信群二维码的链接
    private Date openDate; // 开营日期
    private String broadcastUrl; //红点链接
    private String broadcastRoomNo; //红点房间号
    private String broadcastPassword; //红点密码
    private String qqGroup; //qq群二维码的链接
    private String qqGroupNo; //qq群号
}
