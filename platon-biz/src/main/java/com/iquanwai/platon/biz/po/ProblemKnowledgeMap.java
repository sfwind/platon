package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ProblemKnowledgeMap {
    private int id;
    private Integer problemId; //问题id
    private Integer knowledgeId; //知识点id
    private Integer weight; //知识点在问题中所占权重（百分比）
}
