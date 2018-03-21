package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
@ApiModel("用户信息")
public class ProfileDto {
    @ApiModelProperty("行业")
    private String industry;
    @ApiModelProperty("职业")
    private String function;
    @ApiModelProperty("工作年限")
    private String workingLife;
    @ApiModelProperty("参加工作年份")
    private String workingYear;
    @ApiModelProperty("工作年份id")
    private Integer workingTimeId;
    @ApiModelProperty("城市")
    private String city;
    @ApiModelProperty("城市id")
    private Integer cityId;
    @ApiModelProperty("省份")
    private String province;
    @ApiModelProperty("省份id")
    private Integer provinceId;
    @ApiModelProperty("是否已经填完整")
    private Boolean isFull;
    @ApiModelProperty("是否绑定手机号或微信")
    private Boolean bindMobile;
    @ApiModelProperty("真名")
    private String realName;
    @ApiModelProperty("收件地址")
    private String address;
    @ApiModelProperty("电话")
    private String phone;
    @ApiModelProperty("微信id")
    private String weixinId;
    @ApiModelProperty("收件人")
    private String receiver;
    @ApiModelProperty("婚恋情况")
    private String married;
}
