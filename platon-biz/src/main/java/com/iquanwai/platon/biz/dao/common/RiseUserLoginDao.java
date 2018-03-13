package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.RiseUserLogin;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文
 */
@Repository
public class RiseUserLoginDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<RiseUserLogin> loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseUserLogin WHERE ProfileId = ? ORDER BY LoginDate DESC";
        ResultSetHandler<List<RiseUserLogin>> h = new BeanListHandler<RiseUserLogin>(RiseUserLogin.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
