package com.iquanwai.platon.web.fragmentation.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("首页对象")
public class ApplySuccessDto {

    @ApiModelProperty("申请通过的会员id")
    private Integer goPayMemberTypeId;

    @ApiModelProperty("是否显示商学院申请通过通知")
    private Boolean isShowPassNotify;

    @ApiModelProperty("申请有效期剩余时间")
    private Long remainTime;


    @ApiModelProperty("要申请的商品名字")
    private String name;
}
