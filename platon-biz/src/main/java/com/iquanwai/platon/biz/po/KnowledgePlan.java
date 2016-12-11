package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/5.
 */
@Data
public class KnowledgePlan {
    private int id;
    private Integer planId; //训练计划id
    private Integer knowledgeId; //知识点id
    private Boolean appear; //是否出现过
}
