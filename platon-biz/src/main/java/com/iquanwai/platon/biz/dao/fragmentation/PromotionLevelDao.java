package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.PromotionLevel;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by xfduan on 2017/7/14.
 */
@Repository
public class PromotionLevelDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 插入用户推广层级数据
     */
    public void insertPromotionLevel(String openId, Integer level) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO PromotionLevel (OpenId, Level) VALUES (?, ?)";
        try {
            runner.insert(sql, new ScalarHandler<>(), openId, level);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 根据 OpenId 获取 PromotionLevel
     */
    public PromotionLevel loadByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionLevel WHERE OpenId = ?";
        ResultSetHandler<PromotionLevel> h = new BeanHandler<>(PromotionLevel.class);
        try {
            return runner.query(sql, h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }


}
