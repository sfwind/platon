package com.iquanwai.platon.biz.po.interlocution;

import lombok.Data;

import java.util.Date;

@Data
public class InterlocutionQuestion {
    private Integer id;
    private String topic; //标题
    private Integer profileId; //提问者id
    private Integer followCount; //关注数
    private Integer openCount; //打开数
    private Integer weight; //排序权重
    private Date interlocutionDate; // 问答批次
    private Date addTime; //添加时间
    private Date lastModifiedTime;

    /** 非DB字段，业务字段 **/
    private String answerTips; // 成为第一个回答者／。。等n人回答了问题
    private String addTimeStr; // 添加时间字符串
    private String authorUserName; // 作者名字
    private String authorHeadPic; // 作者头像
    private Boolean follow; // 是否关注
    private Boolean mine; // 是否是自己的问题
    private Boolean answered; // 是否已经回答
    private InterlocutionAnswer answer;
    private InterlocutionDate dateInfo;
    private InterlocutionDate nextDate;
    private InterlocutionAnswer recentlyAnswer; // 最近的圈圈答案
}
