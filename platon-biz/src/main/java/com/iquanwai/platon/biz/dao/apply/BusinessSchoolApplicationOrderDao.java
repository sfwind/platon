package com.iquanwai.platon.biz.dao.apply;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplicationOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2017/11/30.
 */
@Repository
public class BusinessSchoolApplicationOrderDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(BusinessSchoolApplicationOrder businessSchoolApplicationOrder) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessSchoolApplicationOrder (OrderId, ProfileId) " +
                "VALUES (?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    businessSchoolApplicationOrder.getOrderId(),
                    businessSchoolApplicationOrder.getProfileId());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public BusinessSchoolApplicationOrder loadBusinessSchoolApplicationOrder(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplicationOrder WHERE OrderId = ?";
        ResultSetHandler<BusinessSchoolApplicationOrder> h = new BeanHandler<>(BusinessSchoolApplicationOrder.class);
        try {
            return runner.query(sql, h, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public BusinessSchoolApplicationOrder loadBusinessSchoolApplicationOrder(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplicationOrder WHERE ProfileId = ? and Paid=1 and Del = 0";
        ResultSetHandler<BusinessSchoolApplicationOrder> h = new BeanHandler<>(BusinessSchoolApplicationOrder.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void paid(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update BusinessSchoolApplicationOrder SET Paid = 1 WHERE OrderId = ?";
        try {
            runner.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public BusinessSchoolApplicationOrder loadUnAppliedOrder(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplicationOrder WHERE ProfileId = ? and Paid=1 and Del = 0 and Applied = 0";
        ResultSetHandler<BusinessSchoolApplicationOrder> h = new BeanHandler<>(BusinessSchoolApplicationOrder.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void applied(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update BusinessSchoolApplicationOrder SET Applied = 1 WHERE OrderId = ?";
        try {
            runner.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
