package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-08
 */
@Data
public class CourseScheduleTopic {
    private Integer id;
    private Integer category;
    private Integer month;
    private String topic;
    private Boolean del;
}
