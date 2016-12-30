package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemPlan;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/8.
 */
@Repository
public class ProblemPlanDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void saveProblems(List<ProblemPlan> problemPlans){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ProblemPlan(Openid, ProblemId, Status) " +
                "values(?,?,0)";
        try {
            Object[][] param = new Object[problemPlans.size()][];
            for (int i = 0; i < problemPlans.size(); i++) {
                ProblemPlan problemPlan = problemPlans.get(i);
                param[i] = new Object[2];
                param[i][0] = problemPlan.getOpenid();
                param[i][1] = problemPlan.getProblemId();
            }
            runner.batch(sql, param);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ProblemPlan> loadProblems(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemPlan>> h = new BeanListHandler(ProblemPlan.class);
        String sql = "SELECT * FROM ProblemPlan where Openid=? and Status=0";
        try {
            return run.query(sql, h, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void updateStatus(String openid, Integer problemId, Integer status){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ProblemPlan SET STATUS = ? where Openid=? and problemId=?";
        try {
            asyncRun.update(sql, status, openid, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }
}
