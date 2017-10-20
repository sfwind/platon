package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.CustomerMessageLog;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文 on 2017/10/17
 */
@Repository
public class CustomerMessageLogDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(CustomerMessageLog messageLog) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CustomerMessageLog (Openid, PublishTime, Comment, ContentHash, ForwardlyPush, ValidPush) " +
                "VALUES ( ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    messageLog.getOpenId(),
                    messageLog.getPublishTime(),
                    messageLog.getComment(),
                    messageLog.getContentHash(),
                    messageLog.getForwardlyPush(),
                    messageLog.getValidPush());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    // 获取在一定时间段之内的所有记录
    public List<CustomerMessageLog> loadInDistanceDate(String openId, String distanceDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerMessageLog WHERE Date(PublishTime) > ? AND OpenId = ? AND ValidPush = 1";
        ResultSetHandler<List<CustomerMessageLog>> h = new BeanListHandler<>(CustomerMessageLog.class);
        try {
            return runner.query(sql, h, distanceDate, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<CustomerMessageLog> loadInDistanceTime(String openId, String distanceTime) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerMessageLog WHERE PublishTime > ? AND OpenId = ? AND ValidPush = 1";
        ResultSetHandler<List<CustomerMessageLog>> h = new BeanListHandler<>(CustomerMessageLog.class);
        try {
            return runner.query(sql, h, distanceTime, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    // 根据发送内容的 hashCode 来获取模板消息发送记录
    public List<CustomerMessageLog> loadByContentHashCode(String openId, String contentHashCode) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerMessageLog WHERE OpenId = ? AND ContentHash = ? AND ValidPush = 1";
        ResultSetHandler<List<CustomerMessageLog>> h = new BeanListHandler<>(CustomerMessageLog.class);
        try {
            return runner.query(sql, h, openId, contentHashCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
