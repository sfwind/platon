package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class GroupPromotion {

    private Integer id;
    private Integer profileId;
    private String groupCode;
    private Boolean leader;
    private Boolean del;

}
