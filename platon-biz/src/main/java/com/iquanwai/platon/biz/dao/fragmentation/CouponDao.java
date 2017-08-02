package com.iquanwai.platon.biz.dao.fragmentation;

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
import java.util.List;

/**
 * Created by xfduan on 2017/7/14.
 */
@Repository
public class CouponDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insertCoupon(Coupon coupon) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Coupon (OpenId, ProfileId, Amount, Used, ExpiredDate, Description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), coupon.getOpenId(), coupon.getProfileId(),
                    coupon.getAmount(), coupon.getUsed(), coupon.getExpiredDate(), coupon.getDescription());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<Coupon> loadCoupons(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Coupon WHERE ProfileId = ?";
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
