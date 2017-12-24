package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class AnnualSummary {
    private Integer id;
    private Integer profileId;
    private Date joinDate;
    private Integer joinOrder;
    private Integer allRightCount;
    private Integer point;
    private Integer knowledges;
}