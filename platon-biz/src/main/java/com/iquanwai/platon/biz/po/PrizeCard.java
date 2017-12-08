package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class PrizeCard {

    private Integer id;
    private Integer profileId;
    private String prizeCardNo;
    private Boolean used;
    private Boolean del;

    //礼品卡新增字段（2017/12/08）
    private Double amount;
    private String receiverOpenId;
    private Boolean shared;
}
