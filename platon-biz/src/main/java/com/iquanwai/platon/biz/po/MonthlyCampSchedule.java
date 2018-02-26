package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class MonthlyCampSchedule {

    private Integer year; // 年份
    private Integer month; // 月份
    private Integer type; // 小课类型 1-主修 2-辅修
    private Integer problemId; // 小课 Id
    private Boolean del; // 是否删除

    public static final int MAJOR_TYPE = 1;

}
