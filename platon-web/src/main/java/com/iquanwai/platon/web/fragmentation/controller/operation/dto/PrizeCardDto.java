package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import lombok.Data;

@Data
public class PrizeCardDto {
    private String prizeCardNo;//卡号
    private String riseId;//拥有者
    private Boolean used;//是否已领取
}
