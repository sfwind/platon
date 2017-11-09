package com.iquanwai.platon.biz.po.schedule;

import lombok.Data;

/**
 * 选课问题的选项
 *
 * @author nethunder
 * @version 2017-11-04
 */
@Data
public class ScheduleChoice {
    private Integer id;
    private Integer sequence;
    private String subject;
    private Integer questionId;
    private Boolean del;
}
