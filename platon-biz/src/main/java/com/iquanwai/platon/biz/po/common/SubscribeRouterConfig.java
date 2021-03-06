package com.iquanwai.platon.biz.po.common;

import lombok.Data;

@Data
public class SubscribeRouterConfig {

    private Integer id;
    private String url; // 正则 url
    private String scene; // 场景号
    private Integer sequence; // 正则匹配优先级
    private Boolean del;

}
