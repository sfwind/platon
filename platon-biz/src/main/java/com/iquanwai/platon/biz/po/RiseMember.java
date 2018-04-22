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
    private Integer memberTypeId;
    private Date openDate;
    private Date expireDate;
    private Boolean expired;
    private Date addTime;
    private Integer vip;


    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate
    private String name; //非DB字段

    private Boolean expiredInSevenDays; // 会员过期将在7日内过期

    private Boolean showGlobalNotify; // 是否展示全局通知

    public RiseMember simple() {
        RiseMember riseMember = new RiseMember();
        riseMember.setMemberTypeId(memberTypeId);
        riseMember.setExpireDate(expireDate);
        riseMember.setExpired(expired);
        riseMember.setAddTime(addTime);
        riseMember.setOpenDate(openDate);
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
     * 专项课
     */
    public static final int CAMP = 5;
    /**
     * 单买课程
     */
    public static final int COURSE = 6;
    /**
     * 商学院申请
     */
    public static final int BS_APPLICATION = 7;
    /**
     * 商业思维
     */
    public static final int BUSINESS_THOUGHT = 8;
    /**
     * 商业思维申请
     */
    public static final int BUSINESS_THOUGHT_APPLY = 9;

    public static boolean isApply(Integer memberTypeId) {
        return memberTypeId == BS_APPLICATION || memberTypeId == BUSINESS_THOUGHT_APPLY;
    }

    public static boolean isMember(Integer memberTypeId) {
        return memberTypeId == ELITE || memberTypeId == BUSINESS_THOUGHT;
    }
}
