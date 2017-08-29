package com.iquanwai.platon.biz.po.common;

import lombok.Data;

@Data
public class MonthlyCampOrder {

    private Integer id;
    private String orderId; // 主订单 id
    private String openId;  // 用户 openId
    private Integer profileId; // 用户 id
    private Integer month; // 月份
    private Integer entry; // 是否已经报名
    private Integer isDel; // 是否已经过期

}
