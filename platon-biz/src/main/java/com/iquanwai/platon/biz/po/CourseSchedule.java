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
    private Integer profileId;
    private Integer problemId;
    private Integer year;
    private Integer month;
    private Integer type;
    private Boolean del;
    private Date addTime;

    private String topic;
    private Problem problem;

}
