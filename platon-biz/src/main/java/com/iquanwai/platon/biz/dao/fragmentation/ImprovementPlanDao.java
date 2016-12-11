package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ImprovementPlan plan){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ImprovementPlan(Openid, Complete, Status, EndDate, " +
                "StartDate, Score, Total, ReadWizard, ProblemId, Key) " +
                "values(?,?,?,?,?,?,?,?,?,?)";
        try {

            Integer insertRs = runner.insert(sql, new ScalarHandler<>(),
                    plan.getOpenid(), plan.getComplete(), plan.getStatus(),
                    plan.getEndDate(), plan.getStartDate(), plan.getScore(),
                    plan.getTotal(), plan.getReadWizard(), plan.getProblemId(),
                    plan.getKey());
            return insertRs;
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public void readWizard(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET ReadWizard =1 where Id=?";
        try {
            asyncRun.update(sql, planId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ImprovementPlan loadRunningPlan(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid=? and Status = 1";
        ResultSetHandler<ImprovementPlan> h = new BeanHandler(ImprovementPlan.class);
        try {
            ImprovementPlan improvementPlan =runner.query(sql, h, openid);
            return improvementPlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void updateStatus(Integer planId, Integer status){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET Status =? where Id=?";
        try {
            asyncRun.update(sql, status, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
