package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/3/4.
 */
@Repository
public class ProblemScheduleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ProblemSchedule> loadProblemSchedule(Integer problemId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemSchedule>> h = new BeanListHandler<>(ProblemSchedule.class);
        String sql = "SELECT * FROM ProblemSchedule where ProblemId=? and Del=0";
        try {
            return run.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public ProblemSchedule loadByKnowledgeId(Integer knowledgeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ProblemSchedule WHERE KnowledgeId = ? AND Del = 0";
        ResultSetHandler<ProblemSchedule> h = new BeanHandler<>(ProblemSchedule.class);
        try {
            return runner.query(sql, h, knowledgeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
