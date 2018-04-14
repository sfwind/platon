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
    @ApiModelProperty("地址")
    private String address;
    @ApiModelProperty("收件人手机号码")
    private String mobileNo;
    @ApiModelProperty("微信id")
    private String weixinId;
    @ApiModelProperty("收件人")
    private String receiver;
    @ApiModelProperty("婚恋情况")
    private String married;
    @ApiModelProperty("学号")
    private String memberId;
    @ApiModelProperty("班级")
    private String className;
    @ApiModelProperty("会员类型")
    private Integer memberTypeId;
    @ApiModelProperty("圈外id")
    private String riseId;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("头像")
    private String headImgUrl;
    @ApiModelProperty("用户手机号码")
    private String mobile;
    @ApiModelProperty("毕业院校")
    private String college;
    @ApiModelProperty("个人简介")
    private String introduction;
    @ApiModelProperty("是否显示收件信息")
    private Boolean isShowInfo;
    @ApiModelProperty("公司")
    private String company;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("score")
    private Integer score;
    @ApiModelProperty("是否能提交")
    private Boolean canSubmit;
    @ApiModelProperty("提交比例")
    private Integer rate;
}
