package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class ArticlesFlow {

    private Integer id;
    private String title;
    private String description;
    private String content;
    private String thumbnail;
    private String linkParam;
    private Boolean del;

}
