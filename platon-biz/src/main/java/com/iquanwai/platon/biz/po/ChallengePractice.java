package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ChallengePractice {
    private int id;
    private String description; // 题干
    private String pic; // 图片链接
    private Integer problemId; //问题id
    private String pcurl; //pc端url 非db字段
    private String content; //提交内容 非db字段
    private Integer submitId; // 提交id，非db字段
    private String submitUpdateTime;// 非db字段

    private Integer voteCount; // 点赞数,非db字段
    private Integer commentCount; // 评论数,非db字段
    private Integer voteStatus; // 点赞状态，是否可以点赞，非db字段
    private List<String> picList; // 图片列表，非db字段
    private Integer planId; // 计划id，非db字段
}
