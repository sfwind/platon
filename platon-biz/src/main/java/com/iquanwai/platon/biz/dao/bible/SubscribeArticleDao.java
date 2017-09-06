package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
@Repository
public class SubscribeArticleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<SubscribeArticle> load(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeArticle LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<SubscribeArticle>(SubscribeArticle.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
