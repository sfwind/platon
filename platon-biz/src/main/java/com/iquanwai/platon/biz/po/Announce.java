package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("公告信息")
public class Announce {

    private Integer id;
    @ApiModelProperty("身份类型，0 - 无 RiseMember 记录")
    private Integer memberTypeId;
    @ApiModelProperty("提示信息")
    private String message;
    @ApiModelProperty("生效时间")
    private Date startTime;
    @ApiModelProperty("失效时间")
    private Date endTime;
    private Boolean del;

}
