package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * @version 2017/9/27
 */
@Data
public class BusinessSchoolApplication {
    private Integer id;
    private Integer submitId;
    private Integer profileId;
    private Integer status;
    private Date checkTime;
    private Double coupon;
    private Boolean deal;
    private Integer originMemberType;
    private Boolean del;
    private Boolean isDuplicate;
    private String comment;
    private Date submitTime;
    private Date dealTime;
    private String orderId;
    private String originMemberTypeName;
    private Integer lastVerified;

    public static final int APPLYING = 0;
    public static final int APPROVE = 1;
    public static final int REJECT = 2;
    public static final int IGNORE = 3;
    public static final int AUTO_CLOSE = 4;

}
