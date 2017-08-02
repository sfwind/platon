package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Coupon;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
public class RiseDto {
    private String riseId;
    private String memberType;
    private String mobile;
    private Boolean isRiseMember;


    // 优惠券信息
    private List<Coupon> coupons;
}
