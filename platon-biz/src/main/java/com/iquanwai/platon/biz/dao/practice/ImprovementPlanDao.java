package com.iquanwai.platon.biz.dao.practice;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ImprovementPlan plan){
        try {
            QueryRunner runner = new QueryRunner(getDataSource());
            String sql = "insert into ImprovementPlan(Openid, Complete, Status, EndDate, " +
                    "StartDate, Score, Total, ReadWizard, ProblemId) " +
                    "values(?,?,?,?,?,?,?,?,?)";

            Integer insertRs = runner.insert(sql, new ScalarHandler<>(),
                    plan.getOpenid(), plan.getComplete(), plan.getStatus(),
                    plan.getEndDate(), plan.getStartDate(), plan.getScore(),
                    plan.getTotal(), plan.getReadWizard(), plan.getProblemId());
            return insertRs;
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }
}
