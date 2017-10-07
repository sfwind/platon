package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/8/29.
 */
@Data
public class RiseCertificate {
    private int id;
    private Integer profileId; //用户id
    private String certificateNo; //证书id
    private Integer year; //开营年
    private Integer month; //开营月
    private Integer groupNo; //小组号
    private String problemName; //小课名
    private Integer type; //证书类型（1-优秀班长，2-优秀组长，3-优秀学员，4-优秀团队, 5-毕业证书）
    private Boolean notified; //是否通知（0-否，1-是）
    private Boolean del; //是否删除（0-否，1-是）

    private String name; //证书获得者 非db字段
    private String typeName; //证书类型名称 非db字段
    private String congratulation; //证书描述 非db字段
}
