package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Knowledge {
    private int id;
    private String knowledge; //知识点
    private Integer type; //知识点分类
    private String analysis; //解析
    private String pic; //图片链接
    private String voice; //语音链接
    private Boolean appear; //非db字段,是否出现过

}
