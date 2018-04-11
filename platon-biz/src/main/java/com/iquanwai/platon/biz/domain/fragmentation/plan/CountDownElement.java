package com.iquanwai.platon.biz.domain.fragmentation.plan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel(value = "倒计时对象")
public class CountDownElement {

    @ApiModelProperty("身份类别信息")
    private Integer memberTypeId;
    @ApiModelProperty("项目描述")
    private String description;
    @ApiModelProperty("剩余天数")
    private String remainCount;

}
