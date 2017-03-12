package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by nethunder on 2017/3/10.
 */
@Data
public class ArticleLabel {
    private Integer id;
    private Integer labelId;
    private Integer articleModule;
    private Integer articleId;
    private Boolean del;
}
