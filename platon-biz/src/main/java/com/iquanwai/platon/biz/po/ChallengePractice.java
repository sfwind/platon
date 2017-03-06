package com.iquanwai.platon.biz.po;

import lombok.Data;

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
}
