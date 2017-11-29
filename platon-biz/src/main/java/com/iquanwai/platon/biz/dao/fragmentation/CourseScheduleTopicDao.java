package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CourseScheduleTopic;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class CourseScheduleTopicDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public List<CourseScheduleTopic> loadAll() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseScheduleTopic WHERE Del = 0";
        ResultSetHandler<List<CourseScheduleTopic>> h = new BeanListHandler<>(CourseScheduleTopic.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
