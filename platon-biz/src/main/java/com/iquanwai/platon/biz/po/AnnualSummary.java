package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class AnnualSummary {
    private Integer id;
    private Integer profileId;
    /**
     * 加入时间
     */
    private Date joinDate;
    /**
     * 第几个学员
     */
    private Integer joinOrder;
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
    private Integer knowledges;
}