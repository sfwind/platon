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
    /**
     * 用户id
     */
    private Integer profileId;
    /**
     * 小课id
     */
    private Integer problemId;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 课程类型（1-主修,2-辅修）
     */
    private Integer type;
    /**
     * 是否推荐
     */
    private Boolean recommend;
    /**
     * 是否选中状态
     */
    private Boolean selected;
    /**
     * 是否删除
     */
    private Boolean del;
    /**
     * 添加时间
     */
    private Date addTime;

    /**
     * 非db字段
     */
    private String topic;
    /**
     * 非db字段
     */
    private Problem problem;
}
