package com.iquanwai.platon.biz.po.common;

import lombok.Data;

@Data
public class SubscribePush {
    private Integer id;
    private String openid;
    private String callbackUrl;
}
