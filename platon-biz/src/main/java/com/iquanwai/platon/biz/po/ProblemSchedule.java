package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/3/4.
 */
@Data
public class ProblemSchedule {
    private int id;
    private Integer problemId; //小课id
    private Integer section; //小课第几节
    private Integer knowledgeId; //知识点id
    private Integer chapter; //小课第几章
    private Integer series; //序号


    private String chapterStr; // 非DB字段，章名
    private String sectionStr; // 非DB字段，节名
}
