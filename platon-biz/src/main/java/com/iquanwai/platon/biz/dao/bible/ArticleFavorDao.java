package com.iquanwai.platon.biz.dao.bible;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.ArticleFavor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/9/6.
 */
@Repository
public class ArticleFavorDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insert(ArticleFavor articleFavor) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ArticleFavor(ProfileId, ArticleId, Favor) VALUES(?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), articleFavor.getProfileId(), articleFavor.getArticleId(), articleFavor.getFavor()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public ArticleFavor load(Integer profileId,Integer articleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ArticleFavor WHERE ProfileId = ? and ArticleId = ?";
        try {
            return runner.query(sql, new BeanHandler<ArticleFavor>(ArticleFavor.class), profileId, articleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer disfavor(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ArticleFavor SET Favor = 0 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer refavor(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ArticleFavor SET Favor = 1 where Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
