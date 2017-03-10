package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ArticleLabel;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/3/10.
 */
@Repository
public class ArticleLabelDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ArticleLabel> loadArticleLabels(Integer moduleId,Integer articleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ArticleLabel where ArticleId = ? and ArticleModule = ?";
        try{
            ResultSetHandler<List<ArticleLabel>> h = new BeanListHandler<ArticleLabel>(ArticleLabel.class);
            return runner.query(sql, h, articleId, moduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ArticleLabel> loadArticleActiveLabels(Integer moduleId,Integer articleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ArticleLabel where ArticleId = ? and ArticleModule = ? and Del = 0";
        try{
            ResultSetHandler<List<ArticleLabel>> h = new BeanListHandler<ArticleLabel>(ArticleLabel.class);
            return runner.query(sql, h, articleId, moduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int insertArticleLabel(Integer moduleId,Integer articleId,Integer labelId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ArticleLabel(LabelId, ArticleModule, ArticleId) VALUES (?,?,?)";
        try{
            return runner.insert(sql, new ScalarHandler<Long>(), labelId, moduleId, articleId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

    public boolean updateDelStatus(Integer id,Integer del){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ArticleLabel SET Del = ? where Id = ?";
        try{
            runner.update(sql,del,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
            return false;
        }
        return true;
    }
}
