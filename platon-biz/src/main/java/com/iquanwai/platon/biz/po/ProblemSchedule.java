package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/3/4.
 */
@Data
public class ProblemSchedule {
    private int id;
    private Integer problemId; //专题id
    private Integer series; //专题的第几节
    private Integer knowledgeId; //知识点id
    private Integer sequence; //知识点id出现的顺序
}
