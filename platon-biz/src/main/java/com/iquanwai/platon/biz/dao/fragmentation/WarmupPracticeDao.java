package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.WarmupPractice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class WarmupPracticeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<WarmupPractice> loadPractice(int knowledgeId, int problemId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPractice>> h = new BeanListHandler(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where KnowledgeId=? and ProblemId=?";
        try {
            return run.query(sql, h, knowledgeId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<WarmupPractice> loadExample(int knowledgeId, int problemId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPractice>> h = new BeanListHandler(WarmupPractice.class);
        String sql = "SELECT * FROM WarmupPractice where KnowledgeId=? and ProblemId=? and Example=1";
        try {
            return run.query(sql, h, knowledgeId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
