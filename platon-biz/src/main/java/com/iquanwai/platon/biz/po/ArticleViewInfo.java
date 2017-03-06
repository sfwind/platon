package com.iquanwai.platon.biz.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/3/5.
 */
@Data
@NoArgsConstructor
public class ArticleViewInfo {
    private Integer id;
    private Integer viewEventType;
    private Integer articleModule;
    private Integer articleId;
    private Integer count;

    public ArticleViewInfo(Integer articleModule, Integer articleId, Integer viewEventType) {
        this.articleModule = articleModule;
        this.viewEventType = viewEventType;
        this.articleId = articleId;
    }
}
