package com.iquanwai.platon.biz.po.apply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * @version 2017/9/27
 */
@Data
@ApiModel("用户商学院申请表")
public class BusinessSchoolApplication {
    private Integer id;
    @ApiModelProperty("提交的id")
    private Integer submitId;
    @ApiModelProperty("用户编号")
    private Integer profileId;
    @ApiModelProperty("审批状态")
    private Integer status;
    @ApiModelProperty("审核时间")
    private Date checkTime;
    @ApiModelProperty("优惠券金额")
    private Double coupon;
    @ApiModelProperty("是否处理")
    private Boolean deal;
    @ApiModelProperty("原来的会员类型")
    private Integer originMemberType;
    private Boolean del;
    @ApiModelProperty("是否重复申请")
    private Boolean isDuplicate;
    @ApiModelProperty("评价")
    private String comment;
    @ApiModelProperty("申请提交时间")
    private Date submitTime;
    @ApiModelProperty("发送通知时间")
    private Date dealTime;
    @ApiModelProperty("预约申请orderId")
    private String orderId;
    @Deprecated
    private String originMemberTypeName;
    @ApiModelProperty("上次审批结果")
    private Integer lastVerified;
    @ApiModelProperty("是否有效")
    private Boolean valid;

    public static final int APPLYING = 0;
    public static final int APPROVE = 1;
    public static final int REJECT = 2;
    public static final int IGNORE = 3;
    public static final int AUTO_CLOSE = 4;

}
