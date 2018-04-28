package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PracticePlan;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<PracticePlan> loadNeverUnlockPlan(List<Integer> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PracticePlan WHERE PlanId in (" + produceQuestionMark(planIds.size()) + ") AND Status = 2 And Del=0";
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        try {
            return runner.query(sql, h, planIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<PracticePlan> loadByPlanIds(List<Integer> planIds){
        if(CollectionUtils.isEmpty(planIds)){
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PracticePlan WHERE PlanId in (" +produceQuestionMark(planIds.size())+") AND DEL = 0";
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);

        try {
            return runner.query(sql,h,planIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public void batchInsert(List<PracticePlan> planList) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into PracticePlan(PracticeId, PlanId, Type, Unlocked, Status, KnowledgeId, Sequence, Series) " +
                "values(?,?,?,?,?,?,?,?)";
        try {
            Object[][] param = new Object[planList.size()][];
            for (int i = 0; i < planList.size(); i++) {
                PracticePlan practicePlan = planList.get(i);
                param[i] = new Object[8];
                param[i][0] = practicePlan.getPracticeId();
                param[i][1] = practicePlan.getPlanId();
                param[i][2] = practicePlan.getType();
                param[i][3] = practicePlan.getUnlocked();
                param[i][4] = practicePlan.getStatus();
                param[i][5] = practicePlan.getKnowledgeId();
                param[i][6] = practicePlan.getSequence();
                param[i][7] = practicePlan.getSeries();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<PracticePlan> loadPracticePlan(Integer planId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? AND Del = 0 Order by Series";
        try {
            return run.query(sql, h, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public PracticePlan loadPracticePlan(Integer planId, Integer practiceId, Integer type) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? and PracticeId=? and Type=? and Del = 0";
        try {
            return run.query(sql, h, planId, practiceId, type);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public PracticePlan loadChallengePractice(Integer planId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? and Type=? and Del = 0";
        try {
            return run.query(sql, h, planId, PracticePlan.CHALLENGE);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public PracticePlan loadProblemIntroduction(Integer planId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId = ? and Type = ? and Del = 0";
        try {
            return run.query(sql, h, planId, PracticePlan.INTRODUCTION);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public PracticePlan loadApplicationPractice(Integer planId, Integer practiceId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<PracticePlan> h = new BeanHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=? and PracticeId=? and Type in (?,?,?)" +
                " and Del = 0";
        try {
            return run.query(sql, h, planId, practiceId, PracticePlan.APPLICATION_BASE,
                    PracticePlan.APPLICATION_UPGRADED, PracticePlan.APPLICATION_GROUP);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void complete(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PracticePlan set Status=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unlock(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PracticePlan set UnLocked=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 将某个 plan 下的所有节数全部解锁
     */
    public void batchUnlockByPlanId(Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE PracticePlan SET UnLocked = 1 WHERE PlanId = ?";
        try {
            runner.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 将某个 plan 下的所有不能解锁的小节都设置为未完成
     */
    public void revertNeverUnlockPracticePlan(Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE PracticePlan SET Status = 0 WHERE PlanId = ? and Status = 2";
        try {
            runner.update(sql, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<PracticePlan> loadBySeries(Integer planId, Integer series) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan WHERE PlanId = ? and Series = ? AND Del = 0 ORDER BY Sequence ASC";
        try {
            return runner.query(sql, h, planId, series);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<PracticePlan> loadApplicationPracticeByPlanId(Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId = ? and Type in (?,?,?) and Del = 0";
        try {
            List<PracticePlan> practicePlans = runner.query(sql, h, planId, PracticePlan.APPLICATION_BASE,
                    PracticePlan.APPLICATION_UPGRADED, PracticePlan.APPLICATION_GROUP);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<PracticePlan> loadWarmupPracticeByPlanId(Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId = ? and Type in (?,?) and Del = 0";
        try {
            List<PracticePlan> practicePlans = runner.query(sql, h, planId, PracticePlan.WARM_UP,
                    PracticePlan.WARM_UP_REVIEW);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<PracticePlan> loadKnowledgeAndWarmupPracticePlansByPlanId(Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId = ? and Type in (?,?,?,?,?,?,?) and Del = 0";
        try {
            List<PracticePlan> practicePlans = runner.query(sql, h, planId,PracticePlan.PREVIEW,PracticePlan.WARM_UP,
                    PracticePlan.WARM_UP_REVIEW, PracticePlan.INTRODUCTION, PracticePlan.CHALLENGE,
                    PracticePlan.KNOWLEDGE, PracticePlan.KNOWLEDGE_REVIEW);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
