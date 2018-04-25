package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

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
    @ApiModelProperty("课程积分")
    private Integer point;
    @ApiModelProperty("是否是专业版用户")
    private Boolean isProMember;
    @ApiModelProperty("是否展示分享商学院")
    private Boolean showShare;
    @ApiModelProperty("会员类型及到期时间")
    private List<String> memberExpiredDate;
}

