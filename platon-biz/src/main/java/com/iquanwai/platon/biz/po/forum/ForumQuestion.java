package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumQuestion {
    private int id;
    private String topic; //标题
    private String description; //问题详情
    private Integer profileId; //提问者id
    private Integer followCount; //关注数
    private Integer openCount; //打开数
    private Integer answerCount; //回答数
    private Integer weight; //排序权重
    private Date addTime; //添加时间

    /** 非DB字段，业务字段 **/
    private String answerTips; // 成为第一个回答者／。。等n人回答了问题
    private String addTimeStr; // 添加时间字符串
    private String authorUserName; // 作者名字
    private String authorHeadPic; // 作者头像
    private Boolean follow; // 是否关注

    private List<ForumAnswer> answerList; //非db字段 回答列表
    private List<QuestionTag> questionTagList; //非db字段 问题标签
}
