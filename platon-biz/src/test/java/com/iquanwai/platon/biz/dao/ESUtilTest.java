package com.iquanwai.platon.biz.dao;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.forum.ForumQuestionDao;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.repository.ClassA;
import com.iquanwai.platon.biz.repository.elasticsearch.ESManager;
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
    @Autowired
    private ESManager esManager;
    @Autowired
    private ForumQuestionDao forumQuestionDao;

    @Test
    public void esTest(){

    }

    @Test
    public void utilsTest() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("age", "40"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("gender", "M"));
//        "bank", "account",
        SearchResult<ClassA> search = forumQuestionRepository.search(ClassA.class, boolQueryBuilder, new Page());
        List<ClassA> hits = search.getHits();
        for (ClassA hit : hits) {
            System.out.println(JSON.toJSONString(hit));
        }
    }

    @Test
    public void insertTest() {
        ForumQuestion forumQuestion = forumQuestionDao.load(ForumQuestion.class, 62);
    }

    @Test
    public void updateTest() {
        ForumQuestion forumQuestion = forumQuestionDao.load(ForumQuestion.class, 62);
        forumQuestionRepository.update(forumQuestion.getId(), "修改后", forumQuestion.getDescription());
    }

    @Test
    public void indexTest(){
//        List<Column> columns = Lists.newArrayList();
//        Column name = new Column("name", "string", 0);
//        Column age = new Column("age", "number", 0);
//        Column birthday = new Column("birth", "timestamp", 0);
//        columns.add(name);
//        columns.add(age);
//        columns.add(birthday);
//        boolean b = esManager.initIndex("test", "test", columns);
//        System.out.println(b);

    }
}
