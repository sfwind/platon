package com.iquanwai.platon.biz.repository.elasticsearch;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/6/28.
 */
@Data
public class SearchResult {
    private List<DocValue> hits;

    public long totalHits;

    private float maxScore;

}
