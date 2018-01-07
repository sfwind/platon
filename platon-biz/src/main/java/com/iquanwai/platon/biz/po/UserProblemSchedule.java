package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 2017/11/11.
 */
@Data
public class UserProblemSchedule {
    private int id;
    private Integer section; //课程第几节
    private Integer knowledgeId; //知识点id
    private Integer chapter; //课程第几章
    private Integer series; //序号
    private Integer planId; //计划id

    private String chapterStr; // 非DB字段，章名
    private String sectionStr; // 非DB字段，节名
}
