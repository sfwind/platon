package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class ArticleFavor {
    private Integer id;
    private Integer profileId;
    private Integer articleId;
    /** 是否喜欢 */
    private Boolean favor;
}