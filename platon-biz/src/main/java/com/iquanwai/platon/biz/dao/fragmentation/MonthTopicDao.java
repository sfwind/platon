package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.MonthTopic;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class MonthTopicDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public List<MonthTopic> loadAll() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthTopic WHERE Del = 0";
        ResultSetHandler<List<MonthTopic>> h = new BeanListHandler<>(MonthTopic.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
