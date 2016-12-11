package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemList;
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
public class ProblemListDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void saveProblems(List<ProblemList> problemListList){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "insert into ProblemList(Openid, ProblemId, Status) " +
                "values(?,?,0)";
        try {
            Object[][] param = new Object[problemListList.size()][];
            for (int i = 0; i < problemListList.size(); i++) {
                ProblemList problemList = problemListList.get(i);
                param[i] = new Object[2];
                param[i][0] = problemList.getOpenid();
                param[i][1] = problemList.getProblemId();
            }
            asyncRun.batch(sql, param);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ProblemList> loadProblems(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemList>> h = new BeanListHandler(ProblemList.class);
        String sql = "SELECT * FROM ProblemList where Openid=?";
        try {
            List<ProblemList> problemLists = run.query(sql, h, openid);
            return problemLists;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
