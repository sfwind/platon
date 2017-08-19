package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/8/16.
 * 活动减免配置
 */
@Data
public class CourseReductionActivity {
    private Integer id;
    private String activity;
    private Double price;
    private String comment;
    private Integer problemId;
    private Boolean del;
    private Date addTime;
}
