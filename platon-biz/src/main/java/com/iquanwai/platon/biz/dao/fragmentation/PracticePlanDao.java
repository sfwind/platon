package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PracticePlan;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void batchInsert(List<PracticePlan> planList){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into PracticePlan(PracticeId, PlanId, Type, Unlocked, Status, KnowledgeId, Sequence, Series, Summary) " +
                "values(?,?,?,?,?,?,?,?,?)";
        try {
            Object[][] param = new Object[planList.size()][];
            for (int i = 0; i < planList.size(); i++) {
                PracticePlan practicePlan = planList.get(i);
                param[i] = new Object[9];
                param[i][0] = practicePlan.getPracticeId();
                param[i][1] = practicePlan.getPlanId();
                param[i][2] = practicePlan.getType();
                param[i][3] = practicePlan.getUnlocked();
                param[i][4] = practicePlan.getStatus();
                param[i][5] = practicePlan.getKnowledgeId();
                param[i][6] = practicePlan.getSequence();
                param[i][7] = practicePlan.getSeries();
                param[i][8] = practicePlan.getSummary();
            }
            runner.batch(sql, param);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<PracticePlan> loadPracticePlan(Integer planId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? Order by Series";
        try {
            List<PracticePlan> practicePlans = run.query(sql, h,
                    planId);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public PracticePlan loadPracticePlan(Integer planId, String practiceId, Integer type){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? and PracticeId=? and Type=?";
        try {
            PracticePlan practicePlan = run.query(sql, h,
                    planId, practiceId, type);
            return practicePlan;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void complete(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "update PracticePlan set Status=1 where Id=?";
        try {
            asyncRun.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unlock(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "update PracticePlan set UnLocked=1 where Id=?";
        try {
            asyncRun.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void summary(Integer planId, Integer series){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "update PracticePlan set Summary=1 where PlanId=? and Series=?";
        try {
            asyncRun.update(sql, planId, series);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
