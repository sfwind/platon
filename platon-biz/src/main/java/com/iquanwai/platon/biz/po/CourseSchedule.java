package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * @version 2017-11-03
 */
@Data
public class CourseSchedule {
    private Integer id;
    private Integer profileId; //用户id
    private Integer problemId; //小课id
    private Integer year; //年份
    private Integer month; //月份
    private Integer type; //课程类型（1-主修,2-辅修）
    private Boolean del; //是否删除
    private Boolean selected; //是否选择(0-否,1-是)
    private Date addTime; //添加时间

    private String topic; //非db字段
    private Problem problem; //非db字段
}
