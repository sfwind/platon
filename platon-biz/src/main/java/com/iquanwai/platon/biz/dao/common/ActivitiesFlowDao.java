package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文
 */
@Repository
public class ActivitiesFlowDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int downLine(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ActivitiesFlow SET Status = 2 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
