package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文 on 2017/10/21
 */
@Repository
public class MonthlyCampConfigDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MonthlyCampConfig loadActiveMonthlyCampConfig() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthlyCampConfig WHERE Active = 1";
        ResultSetHandler<MonthlyCampConfig> h = new BeanHandler<>(MonthlyCampConfig.class);

        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
