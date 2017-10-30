package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.SubscribePush;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class SubscribePushDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SubscribePush loadById(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<SubscribePush> h = new BeanHandler<>(SubscribePush.class);
        String sql = "SELECT * FROM SubscribePush where Id=? order by Id desc";
        try {
            return run.query(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insert(String openid, String callback, String scene) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        String sql = "INSERT  INTO SubscribePush(Openid, CallbackUrl,Scene) VALUES(? , ?, ?)";
        try {
            return runner.insert(sql, h, openid, callback, scene).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
