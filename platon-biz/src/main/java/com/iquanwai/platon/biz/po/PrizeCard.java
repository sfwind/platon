package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class PrizeCard {
    private int id;
    private Integer profileId;  //拥有者
    private String prizeCardNo; //卡号
    private Boolean used; //是否使用
    private Integer category; //礼品卡类别
    private String receiverProfileId; //领取者id
    private String description; //礼品卡描述
    private Date expiredTime; //过期时间

    //非db字段
    private String background; //卡片封面url
    private String url; //跳转url
    private String expiredDate; //过期时间
    private boolean expired; //是否过期

}
