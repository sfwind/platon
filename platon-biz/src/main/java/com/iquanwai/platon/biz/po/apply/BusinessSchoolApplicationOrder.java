package com.iquanwai.platon.biz.po.apply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2017/11/30.
 */
@Data
@ApiModel("商学院申请order表")
public class BusinessSchoolApplicationOrder {
    private Integer id;
    @ApiModelProperty("用户编号")
    private Integer profileId;
    @ApiModelProperty("订单编号")
    private String orderId;
    @ApiModelProperty("是否支付")
    private Boolean paid;
    private Boolean del;
}
