package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by nethunder on 2017/8/31.
 */
@Data
public class LiveRedeemCode {
    private Integer id;
    private String code;
    private String live;
    private Boolean used;
    private Integer profileId;
    private Boolean del;
}