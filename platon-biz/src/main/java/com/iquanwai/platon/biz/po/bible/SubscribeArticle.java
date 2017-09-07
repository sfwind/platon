package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class SubscribeArticle {
    private Integer id;
    /** 标题 */
    private String title;
    private String url;
    /** 标签id，用英文逗号分割 */
    private String tag;
    /** 文章来源 */
    private String source;
    /** 字数 */
    private Integer wordCount;
    /** 上传时间 */
    private Date upTime;


    // 非db字段
    /** 是否打开过这篇文章 */
    private Boolean acknowledged;
    /**
     * 是否喜欢<br/>
     * 0 - 未知<br/>
     * 1 - 不喜欢<br/>
     */
    private Integer disfavor;
    /** 标签名字 */
    private List<String> tagNames;

}
