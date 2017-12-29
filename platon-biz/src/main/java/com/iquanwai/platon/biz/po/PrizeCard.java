package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class PrizeCard {

    private Integer id;
    private Integer profileId;
    private String prizeCardNo;
    private Boolean used;
    private Integer category;
    private String receiverProfileId;
    private Boolean del;
}
