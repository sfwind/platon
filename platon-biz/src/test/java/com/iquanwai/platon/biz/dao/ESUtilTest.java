package com.iquanwai.platon.biz.dao;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.repository.ClassA;
import com.iquanwai.platon.biz.repository.elasticsearch.DocValue;
import com.iquanwai.platon.biz.repository.elasticsearch.SearchResult;
import com.iquanwai.platon.biz.repository.forum.ForumQuestionRepository;
import com.iquanwai.platon.biz.util.page.Page;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by nethunder on 2017/6/28.
 */
public class ESUtilTest extends TestBase{
    @Autowired
    private ForumQuestionRepository forumQuestionRepository;

    @Test
    public void esTest(){

    }

    @Test
    public void utilsTest() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("age", "40"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("gender", "M"));
//        "bank", "account",
        SearchResult search = forumQuestionRepository.search(ClassA.class, boolQueryBuilder, new Page());
        List<DocValue> hits = search.getHits();
        for (DocValue hit : hits) {
            System.out.println(JSON.toJSONString(hit));
        }

    }
}
