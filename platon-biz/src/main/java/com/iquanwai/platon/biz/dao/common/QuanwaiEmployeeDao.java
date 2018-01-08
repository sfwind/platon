package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.QuanwaiEmployee;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by 三十文 on 2017/10/17
 */
@Repository
public class QuanwaiEmployeeDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<QuanwaiEmployee> loadAllEmployees() {
        QueryRunner runner = new QueryRunner();
        String sql = "SELECT * FROM QuanwaiEmployee WHERE Del = 0";
        ResultSetHandler<List<QuanwaiEmployee>> h = new BeanListHandler<>(QuanwaiEmployee.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public QuanwaiEmployee loadEmployee(Integer profileId) {
        QueryRunner runner = new QueryRunner();
        String sql = "SELECT * FROM QuanwaiEmployee WHERE ProfileId = ? and del = 0";
        ResultSetHandler<QuanwaiEmployee> h = new BeanHandler<>(QuanwaiEmployee.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
