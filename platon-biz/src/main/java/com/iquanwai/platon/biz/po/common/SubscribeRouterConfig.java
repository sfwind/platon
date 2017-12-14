package com.iquanwai.platon.biz.po.common;

import lombok.Data;

@Data
public class SubscribeRouterConfig {

    private Integer id;
    private String url;
    private String scene;
    private Integer sequence;
    private Boolean del;

}
