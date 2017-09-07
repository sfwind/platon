package com.iquanwai.platon.web.fragmentation.controller.bible;

import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/9/7.
 */
@Data
public class DailyArticleDto {
    private String date;
    private List<SubscribeArticle> articleList;
    private Boolean isPageEnd;
}
