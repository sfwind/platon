package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.PromotionLevel;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by xfduan on 2017/7/14.
 */
@Repository
public class PromotionLevelDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 插入用户推广层级数据
     */
    public void insertPromotionLevel(PromotionLevel promotionLevel) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO  PromotionLevel (ProfileId, PromoterId, Level, Activity, Valid) VALUES (?, ?, ?, ?, ?)";
        try {
            runner.insert(sql, new ScalarHandler<>(),
                    promotionLevel.getProfileId(),
                    promotionLevel.getPromoterId(),
                    promotionLevel.getLevel(),
                    promotionLevel.getActivity(),
                    promotionLevel.getValid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 根据 OpenId 获取 PromotionLevel
     */
    @Deprecated
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

    /**
     * 根据 ProfileId 获取 PromotionLevel
     */
    public PromotionLevel loadByProfileId(Integer profileId, String activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionLevel WHERE ProfileId = ? AND Activity = ?";
        ResultSetHandler<PromotionLevel> h = new BeanHandler<>(PromotionLevel.class);
        try {
            return runner.query(sql, h, profileId, activity);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    // 根据推广人 id 查看当前推广人一共推广了多少人的信息
    public List<PromotionLevel> loadByPromoterId(Integer promoterId, String activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionLevel WHERE PromoterId = ? AND Activity = ?";
        ResultSetHandler<List<PromotionLevel>> h = new BeanListHandler<>(PromotionLevel.class);
        try {
            return runner.query(sql, h, promoterId, activity);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
