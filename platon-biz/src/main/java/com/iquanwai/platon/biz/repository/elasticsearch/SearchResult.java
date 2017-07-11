package com.iquanwai.platon.biz.repository.elasticsearch;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.util.page.Page;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/6/28.
 */
@Data
public class SearchResult<T> {
    private List<T> hits;

    public long totalHits;

    private float maxScore;

    public static SearchResult nonResult(Page page) {
        if (page != null) {
            page.setTotal(0);
        }
        SearchResult result = new SearchResult();
        result.setMaxScore(0);
        result.setTotalHits(0);
        result.setHits(Lists.newArrayList());
        return result;
    }
}
