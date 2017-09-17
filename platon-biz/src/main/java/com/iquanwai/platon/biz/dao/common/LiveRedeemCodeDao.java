package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.LiveRedeemCode;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/8/31.
 */
@Repository
public class LiveRedeemCodeDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public LiveRedeemCode loadValidLiveRedeemCode(String live) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from LiveRedeemCode where Live = ? and Used = 0 and Del = 0 ";
        try {
            return runner.query(sql, new BeanHandler<LiveRedeemCode>(LiveRedeemCode.class), live);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public LiveRedeemCode loadLiveRedeemCode(String live, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from LiveRedeemCode where Live = ? and ProfileId = ? AND Del = 0 order by Id desc limit 1";
        try {
            return runner.query(sql, new BeanHandler<LiveRedeemCode>(LiveRedeemCode.class), live, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void useRedeemCode(Integer id, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update LiveRedeemCode set Used = 1,ProfileId = ? where Id = ?";
        try {
            runner.update(sql, profileId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
