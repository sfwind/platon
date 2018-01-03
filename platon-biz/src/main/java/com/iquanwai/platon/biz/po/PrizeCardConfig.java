package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 2018/1/3.
 */
@Data
public class PrizeCardConfig {
    private int id;
    private Integer categoryId;  //礼品卡类别id
    private String coverPic; //未领取卡片封面url
    private String receivedCoverPic; //已领取卡片封面url
    private String detailUrl; //跳转url

}
