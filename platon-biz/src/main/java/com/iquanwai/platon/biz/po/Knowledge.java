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
    private String analysis; //作用
    private String analysisAudio; //作用语音链接
    private String means; //方法
    private String meansAudio; //方法语音链接
    private String keynote; //要点
    private String keynoteAudio; //要点语音链接
    private String pic; //图片链接
    @Deprecated
    private String voice; //语音链接
    private Integer appear; //非db字段,是否出现过

}
