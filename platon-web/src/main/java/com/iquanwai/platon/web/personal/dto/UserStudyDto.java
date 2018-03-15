package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户学习信息Model")
public class UserStudyDto {

    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("头像url")
    private String headImgUrl;
    @ApiModelProperty("学号")
    private String memberId;
    @ApiModelProperty("班级名称")
    private String className;
    @ApiModelProperty("知识卡张数")
    private Integer cardSum;
    @ApiModelProperty("荣誉证书张数")
    private Integer certificateSum;
    @ApiModelProperty("抵用券总额")
    private Integer couponSum;
}
