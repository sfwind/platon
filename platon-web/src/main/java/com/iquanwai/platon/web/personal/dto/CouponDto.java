package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.Coupon;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2018/2/24.
 */
@Data
public class CouponDto {
    private int total;
    private List<Coupon> coupons;
}
