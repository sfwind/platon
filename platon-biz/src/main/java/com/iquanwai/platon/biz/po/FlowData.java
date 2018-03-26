package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class FlowData {

    @ApiModelProperty("可见权限")
    private String authority;
    @ApiModelProperty("是否可见")
    private Boolean visibility;

}
