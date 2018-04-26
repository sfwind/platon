package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("校友录dto")
public class SchoolFriendDto {

    @ApiModelProperty("头像url")
    private String headImgUrl;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("省份")
    private String province;
    @ApiModelProperty("城市")
    private String city;
    @ApiModelProperty("行业")
    private String industry;
    @ApiModelProperty("所在公司")
    private String company;
    @ApiModelProperty("学号")
    private String memberId;
    @ApiModelProperty
    private String riseId;
}