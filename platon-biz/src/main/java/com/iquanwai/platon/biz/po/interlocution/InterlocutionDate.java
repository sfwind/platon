package com.iquanwai.platon.biz.po.interlocution;

import lombok.Data;

import java.util.Date;

@Data
public class InterlocutionDate {
    private Integer id;
    private String topic;
    private String description;
    private Integer batch;
    private Date startDate;
    private Date endDate;
}
