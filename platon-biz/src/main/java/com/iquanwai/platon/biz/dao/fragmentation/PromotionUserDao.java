package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.PromotionUser;
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
 * Created by xfduan on 2017/7/14.
 */
@Repository
public class PromotionUserDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据 OpenId 加载 PromotionUser 数据
     */
    public PromotionUser loadUserByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<PromotionUser> h = new BeanHandler<>(PromotionUser.class);
        String sql = "SELECT * FROM PromotionUser WHERE Openid = ?";
        try {
            return runner.query(sql, h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 根据 OpenId 跟新用户试用购买信息
     */
    public Integer updateActionByOpenId(String openId, Integer action) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE PromotionUser SET Action = ? WHERE Openid = ?";
        try {
            return runner.update(sql, action, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

    /**
     * 根据推广人 openId 获取所有的新人数据
     */
    public List<PromotionUser> loadUsersBySource(String source) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PromotionUser>> h = new BeanListHandler<>(PromotionUser.class);
        String sql = "SELECT * FROM PromotionUser WHERE Source = ?";
        try {
            return runner.query(sql, h, source);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据推广人的 ProfileId 获取数据
     */
    public List<PromotionUser> loadUsersByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionUser WHERE ProfileId = ?";
        ResultSetHandler<List<PromotionUser>> h = new BeanListHandler<>(PromotionUser.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }


}
