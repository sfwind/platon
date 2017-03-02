package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class WarmupPractice {
    private int id;
    private String question; //题干
    private Integer type; //题型（1-单选题，2-多选题）
    private String analysis; //解析
    private String voice; //语音解析
    private Integer difficulty; //难易度（1-容易，2-普通，3-困难）
    private Integer knowledgeId; //知识点id
    @Deprecated
    private Integer sceneId; //场景id
    private Boolean del; //是否删除(0-否,1-是)
    private Integer problemId; //专题id
    private Integer sequence; //出现顺序
    private Boolean example; //是否例题(0-否,1-是)

    private Integer score; //非db字段 分值
    private List<Choice> choiceList; //所有选项
    private List<WarmupPracticeDiscuss> discussList; //热身训练讨论
    private List<Integer> choice; //用户选择选项

}
