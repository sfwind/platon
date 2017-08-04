package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.List;

@Data
public class Recommendation {

    private Integer id;
    private Integer problemId; // 小课 Id
    private Integer sequence; // 排序
    private String description; // 描述说明
    private String recommendIds; // 推荐 ProblemId 列表，以`、`分隔
    private Boolean del;

    /**
     * 非 db 字段
     */
    private List<Problem> recommendProblems;

}
