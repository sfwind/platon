package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/3/4.
 */
@Data
public class ProblemSchedule {
    private int id;
    private Integer problemId; //专题id
    private Integer section; //专题第几节
    private Integer knowledgeId; //知识点id
    private Integer chapter; //专题第几章
    private Integer series; //序号
}
