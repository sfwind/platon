package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class SubscribeArticleTag {
    private int id;
    private String name; // 标签名
    private Boolean del; //是否删除
    private Integer catalog; //标签类别
    private Boolean chosen; //非db字段 是否选择
}
