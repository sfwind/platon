package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.AnnualSummary;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class AnnualSummaryDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AnnualSummary loadUserAnnualSummary(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM AnnualSummary WHERE ProfileId = ? Del =0";
        BeanHandler<AnnualSummary> h = new BeanHandler<>(AnnualSummary.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
