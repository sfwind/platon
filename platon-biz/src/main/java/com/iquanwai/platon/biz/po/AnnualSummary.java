package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class AnnualSummary {
    private Integer id;
    private Integer profileId;
    private String riseId;
    /**
     * 加入时间
     */
    private Date registerDate;
    /**
     * 第几个学员
     */
    private Integer registerSequence;
    /**
     * 全对次数
     */
    private Integer allRightCount;
    /**
     * 总积分
     */
    private Integer point;
    /**
     * 点开知识点数量
     */
    private Integer knowledgeCount;
    /**
     * 打败人数，百分比
     */
    private Double defeatPercentage;
    /**
     * 开课数量
     */
    private Integer courseCount;
    /**
     * 助教id
     */
    private String asstsProfileIds;
    /**
     * 同学ids
     */
    private String classmatesProfileIds;
}