package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
public class ActivitiesFlow {

    private Integer id;
    private String name;
    private String holder;
    private String location;
    private String thumbnail;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String linkUrl;
    private Boolean del;

}
