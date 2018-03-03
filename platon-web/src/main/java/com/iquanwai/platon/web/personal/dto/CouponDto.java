package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.Coupon;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2018/2/24.
 */
@Data
@ApiModel("优惠券")
public class CouponDto {
    @ApiModelProperty("总金额")
    private int total;
    @ApiModelProperty("优惠券列表")
    private List<Coupon> coupons;
}
