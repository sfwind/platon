package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PrizeCardConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2018/1/3.
 */
@Repository
public class PrizeCardConfigDao extends PracticeDBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());

    public PrizeCardConfig loadPrizeCardConfig(Integer categoryId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PrizeCardConfig WHERE CategoryId = ? AND Del = 0";
        ResultSetHandler<PrizeCardConfig> h = new BeanHandler<>(PrizeCardConfig.class);
        try {
            return runner.query(sql, h, categoryId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
