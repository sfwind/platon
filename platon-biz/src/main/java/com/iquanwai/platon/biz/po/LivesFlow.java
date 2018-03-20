package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
public class LivesFlow {

    private Integer id;
    private String name;
    private String speaker;
    private String speakerDesc;
    private String thumbnail;
    private Date startTime;
    private Date endTime;
    private String linkUrl;
    private Boolean del;


    @ApiModelProperty("开始时间 str")
    private String startTimeStr;

}
