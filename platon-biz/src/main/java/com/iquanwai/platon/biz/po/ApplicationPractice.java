package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ApplicationPractice {
    private int id;
    private String topic; //任务标题
    private String description; // 题干
    private Integer knowledgeId; //知识点id
    private Integer sceneId; //子场景id
    private Integer difficulty; //难易度（1-容易，2-普通，3-困难）
    private String content; //提交内容，非db字段
    private Integer submitId; // 提交id，非db字段
    private String submitUpdateTime;// 非db字段
}
