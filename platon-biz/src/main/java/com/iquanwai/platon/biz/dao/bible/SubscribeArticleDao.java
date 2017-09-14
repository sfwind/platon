package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
@Repository
public class SubscribeArticleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<SubscribeArticle> loadLastArticles(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeArticle Order by UpTime DESC LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(SubscribeArticle.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeArticle> loadCertainDateArticles(Page page, String date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeArticle where UpTime = ?  LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(SubscribeArticle.class), date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeArticle> loadToCertainDateArticles(String date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeArticle where UpTime >= ? and UpTime <= CURRENT_TIMESTAMP order by UpTime desc";
        try {
            return runner.query(sql, new BeanListHandler<>(SubscribeArticle.class), date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int count(String date) {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            return run.query("SELECT count(*) FROM SubscribeArticle where UpTime = ?", h, date).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public Date loadMinDate() {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Date> h = new ScalarHandler<Date>();
        try {
            return run.query("SELECT MIN(UpTime) FROM SubscribeArticle", h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<SubscribeArticle> loadArticles(List<Integer> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(articleIds.size());
        List<Object> params = Lists.newArrayList();
        params.addAll(articleIds);
        String sql = "select * from SubscribeArticle where Id in (" + mask + ")";

        try {
            return runner.query(sql, new BeanListHandler<>(SubscribeArticle.class), params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
