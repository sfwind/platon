package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.UserProblemSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 2017/11/11.
 */
@Repository
public class UserProblemScheduleDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<UserProblemSchedule> loadUserProblemSchedule(Integer planId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<UserProblemSchedule>> h = new BeanListHandler<>(UserProblemSchedule.class);
        String sql = "SELECT * FROM ProblemSchedule where PlanId=? and Del=0";
        try {
            return run.query(sql, h, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void batchInsert(List<UserProblemSchedule> userProblemSchedules){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into UserProblemSchedule(PlanId, KnowledgeId, Chapter, Section, Series) " +
                "values(?,?,?,?,?)";
        try {
            Object[][] param = new Object[userProblemSchedules.size()][];
            for (int i = 0; i < userProblemSchedules.size(); i++) {
                UserProblemSchedule userProblemSchedule = userProblemSchedules.get(i);
                param[i] = new Object[5];
                param[i][0] = userProblemSchedule.getPlanId();
                param[i][1] = userProblemSchedule.getKnowledgeId();
                param[i][2] = userProblemSchedule.getChapter();
                param[i][3] = userProblemSchedule.getSection();
                param[i][4] = userProblemSchedule.getSeries();
            }
            runner.batch(sql, param);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
