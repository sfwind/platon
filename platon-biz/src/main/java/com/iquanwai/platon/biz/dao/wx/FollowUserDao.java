package com.iquanwai.platon.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.Account;
import org.apache.commons.collections.CollectionUtils;
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
 * Created by justin on 16/8/12.
 */
@Repository
public class FollowUserDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Account queryByUnionId(String unionId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM FollowUsers WHERE UnionId = ? AND Del = 0";
        ResultSetHandler<Account> h = new BeanHandler<>(Account.class);
        try {
            return runner.query(sql, h, unionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<Account> queryAccounts(List<String> openids) {
        if (CollectionUtils.isEmpty(openids)) {
            return Lists.newArrayList();
        }
        String questionMarks = produceQuestionMark(openids.size());
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Account>> h = new BeanListHandler<>(Account.class);
        String sql = "SELECT * FROM FollowUsers where Openid in (" + questionMarks + ") AND Del = 0";
        try {
            return run.query(sql, h, openids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
