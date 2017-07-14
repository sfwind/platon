package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by xfduan on 2017/7/14.
 */
@Data
public class Coupon {

    private Integer id;
    private String openId;
    private Integer profileId;
    private Integer amount; // 优惠券金额
    private Integer used; // 是否使用 0-否 1-是
    private String orderId; // 用于订单的 id
    private Integer cost; // 本次订单已消耗的金额
    private Date expiredDate; // 过期日期
    private String category; // 分类
    private String description; // 描述

}
