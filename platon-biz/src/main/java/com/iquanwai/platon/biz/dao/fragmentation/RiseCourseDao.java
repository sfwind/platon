package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseCourseOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
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

    public RiseCourseOrder loadOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseCourseOrder> h = new BeanHandler<>(RiseCourseOrder.class);

        try {
            RiseCourseOrder order = run.query("SELECT * FROM RiseCourseOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public RiseCourseOrder loadEntryOrder(Integer profileId, Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<RiseCourseOrder> h = new BeanHandler<>(RiseCourseOrder.class);

        try {
            RiseCourseOrder order = run.query("SELECT * FROM RiseCourseOrder where Entry=1 and IsDel=0 and ProfileId = ? and ProblemId = ?", h, profileId,problemId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

}
