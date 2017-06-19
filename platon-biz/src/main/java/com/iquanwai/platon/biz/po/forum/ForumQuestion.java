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

    private List<ForumAnswer> answerList; //非db字段 回答列表
}
