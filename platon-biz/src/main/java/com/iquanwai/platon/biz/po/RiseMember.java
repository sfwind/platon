package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/13.
 */
@Data
public class RiseMember {
    private Integer id;
    private Integer profileId;
    private String orderId;
    private String openId;
    private Integer memberTypeId;
    private Date expireDate;
    private Boolean expired;
    private Date addTime;

    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate
    private String name; //非DB字段

    private Boolean expiredInSevenDays; // 会员过期将在7日内过期

    public RiseMember simple() {
        RiseMember riseMember = new RiseMember();
        riseMember.setMemberTypeId(memberTypeId);
        riseMember.setExpireDate(expireDate);
        riseMember.setExpired(expired);
        riseMember.setAddTime(addTime);
        riseMember.setStartTime(startTime);
        riseMember.setEndTime(endTime);
        riseMember.setName(name);
        riseMember.setExpiredInSevenDays(expiredInSevenDays);
        return riseMember;
    }

    /**
     * 专业版半年
     */
    public static final int HALF = 1;
    /**
     * 专业版一年
     */
    public static final int ANNUAL = 2;
    /**
     * 精英版一年
     */
    public static final int ELITE = 3;
    /**
     * 精英版半年
     */
    public static final int HALF_ELITE = 4;
    /**
     * 训练营小课
     */
    public static final int CAMP = 5;
    /**
     * 单买小课
     */
    public static final int COURSE = 6;
}
