package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.SubscribeRouterConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class SubscribeRouterConfigDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<SubscribeRouterConfig> loadAll() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeRouterConfig WHERE Del = 0 ORDER BY Sequence ASC";
        ResultSetHandler<List<SubscribeRouterConfig>> h = new BeanListHandler<>(SubscribeRouterConfig.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
