package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.ActivityPopupMessage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2017/12/13.
 */
@Repository
public class ActivityPopupMessageDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ActivityPopupMessage load(Integer profileId, String eventKey) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ActivityPopupMessage WHERE ProfileId = ? and EventKey = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(ActivityPopupMessage.class), profileId, eventKey);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
