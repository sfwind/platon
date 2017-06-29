package com.iquanwai.platon.biz.repository.elasticsearch;

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

}
