package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseCourse;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/7/14.
 */
@Repository
public class RiseCourseDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(RiseCourse riseCourse) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseCourse(ProfileId, Openid, ProblemId, OrderId) " +
                " VALUES (?, ?, ?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    riseCourse.getProfileId(), riseCourse.getOpenid(), riseCourse.getProblemId(), riseCourse.getOrderId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseCourse loadOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseCourse> h = new BeanHandler<>(RiseCourse.class);

        try {
            RiseCourse order = run.query("SELECT * FROM RiseCourse where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public RiseCourse loadOrder(Integer profileId, Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseCourse> h = new BeanHandler<>(RiseCourse.class);

        try {
            RiseCourse order = run.query("SELECT * FROM RiseCourse where ProfileId=? and ProblemId=？ ", h, profileId, problemId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

}
