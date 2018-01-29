package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class ArticleLabel {
    private Integer id;
    private Integer labelId;
    private Integer articleModule;
    private Integer articleId;
    private Boolean del;
}
