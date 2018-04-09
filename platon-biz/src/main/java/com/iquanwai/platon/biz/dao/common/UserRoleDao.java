package com.iquanwai.platon.biz.dao.common;


import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.UserRole;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class UserRoleDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<UserRole> getRoles(Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);
        String sql = "SELECT * FROM UserRole where ProfileId=? and Del=0";
        try {
            return run.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public UserRole getAssist(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserRole> h = new BeanHandler<>(UserRole.class);
        String sql = "SELECT * FROM UserRole where ProfileId=? and RoleId in (3,4,5,6,11,12,13,14,15) and Del=0";
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<UserRole> loadUserRoleByRoleIds(List<Integer> roleIds) {
        if (roleIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM UserRole WHERE RoleId in (" + produceQuestionMark(roleIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<UserRole>(UserRole.class);
        try {
            return runner.query(sql, h, roleIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
