package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.Coupon;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by xfduan on 2017/7/14.
 */
@Repository
public class CouponDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insertCoupon(Coupon coupon) {
        coupon.setUsed(0);
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Coupon (ProfileId, Amount, Used, ExpiredDate, Category, Description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    coupon.getProfileId(),
                    coupon.getAmount(),
                    coupon.getUsed(),
                    coupon.getExpiredDate(),
                    coupon.getCategory(),
                    coupon.getDescription());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<Coupon> loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Coupon WHERE ProfileId = ? AND Used = 0 AND Del =0 AND ExpiredDate >= ?";
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);
        try {
            return runner.query(sql, h, profileId, new Date());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}