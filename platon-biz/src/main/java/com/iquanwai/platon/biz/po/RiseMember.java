package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/13.
 */
@Data
public class RiseMember {
    private Integer id;
    private String orderId;
    private String openId;
    private Integer memberTypeId;
    private Date expireDate;
    private Boolean expired;
    private Date addTime;

    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate
    private String name; //非DB字段

    public static final int ELITE = 3;
}
