package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.Coupon;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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

}
