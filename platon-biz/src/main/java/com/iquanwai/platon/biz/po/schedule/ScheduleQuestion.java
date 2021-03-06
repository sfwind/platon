package com.iquanwai.platon.biz.po.schedule;

import lombok.Data;

import java.util.List;

/**
 * 选课问题的题目
 *
 * @author nethunder
 * @version 2017-11-04
 */
@Data
public class ScheduleQuestion {
    private Integer id;
    private String categoryGroup;
    private Integer sequence;
    private String question;
    private Boolean del;
    private Boolean multiple;

    /**
     * 非DB字段
     */
    private List<ScheduleChoice> scheduleChoices;
}
