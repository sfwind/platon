package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/3/4.
 */
@Data
public class ProblemSchedule {
    private int id;
    private Integer problemId; //课程id
    private Integer section; //课程第几节
    private Integer knowledgeId; //知识点id
    private Integer chapter; //课程第几章
    private Integer series; //序号
}
