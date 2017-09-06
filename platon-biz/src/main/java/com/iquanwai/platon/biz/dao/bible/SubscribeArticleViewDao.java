package com.iquanwai.platon.biz.dao.bible;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleView;
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
public class SubscribeArticleViewDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insert(Integer profileId, Integer articleId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SubscribeArticleView (ProfileId, ArticleId) VALUES (?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), profileId, articleId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public SubscribeArticleView load(Integer profileId,Integer articleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeArticleView WHERE ProfileId = ? and ArticleId = ?";
        try {
            return runner.query(sql, new BeanHandler<SubscribeArticleView>(SubscribeArticleView.class), profileId, articleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
