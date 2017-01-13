package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ImprovementPlan plan){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ImprovementPlan(Openid, WarmupComplete, Status, EndDate, " +
                "StartDate, CloseDate, Point, Total, ApplicationComplete, ProblemId, Keycnt, CurrentSeries, TotalSeries) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    plan.getOpenid(), plan.getWarmupComplete(), plan.getStatus(),
                    plan.getEndDate(), plan.getStartDate(), plan.getCloseDate(),
                    plan.getPoint(), plan.getTotal(), plan.getApplicationComplete(),
                    plan.getProblemId(), plan.getKeycnt(),
                    plan.getCurrentSeries(), plan.getTotalSeries());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public ImprovementPlan loadRunningPlan(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid=? and CloseDate>=? and Status in (1,2)";
        ResultSetHandler<ImprovementPlan> h = new BeanHandler(ImprovementPlan.class);
        try {
            ImprovementPlan improvementPlan =runner.query(sql, h, openid, DateUtils.parseDateToString(new Date()));
            return improvementPlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<ImprovementPlan> loadAllPlans(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid=?";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler(ImprovementPlan.class);
        try {
            List<ImprovementPlan> improvementPlans =runner.query(sql, h, openid);
            return improvementPlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ImprovementPlan getLastPlan(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Openid=? order by id desc";
        ResultSetHandler<ImprovementPlan> h = new BeanHandler(ImprovementPlan.class);
        try {
            ImprovementPlan improvementPlan =runner.query(sql, h, openid);
            return improvementPlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<ImprovementPlan> loadAllRunningPlan(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Status in (1,2)";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler(ImprovementPlan.class);
        try {
            List<ImprovementPlan> improvementPlans =runner.query(sql, h);
            return improvementPlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
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

    public void updateKey(Integer planId, Integer key){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET Keycnt =? where Id=?";
        try {
            asyncRun.update(sql, key, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateProgress(Integer planId, Integer key, Integer series){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET Keycnt =?, CurrentSeries=? where Id=?";
        try {
            asyncRun.update(sql, key, series, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updatePoint(Integer planId, Integer point){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET Point =? where Id=?";
        try {
            asyncRun.update(sql, point, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateApplicationComplete(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET ApplicationComplete = ApplicationComplete+1 where Id=?";
        try {
            asyncRun.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateWarmupComplete(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE ImprovementPlan SET WarmupComplete = WarmupComplete+1 where Id=?";
        try {
            asyncRun.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
