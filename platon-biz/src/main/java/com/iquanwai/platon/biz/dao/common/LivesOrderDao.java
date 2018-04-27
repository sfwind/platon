package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.LivesOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;


/**
 * Created by 三十文
 */
@Repository
public class LivesOrderDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(LivesOrder livesOrder) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO LivesOrder (OrderId, PromotionId, LiveId) VALUES (?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), livesOrder.getOrderId(), livesOrder.getPromotionId(), livesOrder.getLiveId());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public LivesOrder loadByOrderIdAndLiveId(Integer orderId, Integer liveId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM LivesOrder WHERE OrderId = ? AND LiveId = ? AND Del = 0";
        ResultSetHandler<LivesOrder> h = new BeanHandler<LivesOrder>(LivesOrder.class);

        try {
            return runner.query(sql, h, orderId, liveId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
