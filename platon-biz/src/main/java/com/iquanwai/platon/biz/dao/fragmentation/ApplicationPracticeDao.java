package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ApplicationPractice;
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
public class ApplicationPracticeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ApplicationPractice> loadPractice(int knowledgeId, int problemId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String sql = "SELECT * FROM ApplicationPractice where KnowledgeId=? and ProblemId=? and Del=0 order by sequence asc";
        try {
            return run.query(sql, h, knowledgeId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<ApplicationPractice> loadPracticeList(List<Integer> practiceIds){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationPractice>> h = new BeanListHandler<>(ApplicationPractice.class);
        String mask = produceQuestionMark(practiceIds.size());
        List<Object> params = Lists.newArrayList();
        params.addAll(practiceIds);
        String sql = "SELECT * FROM ApplicationPractice where Id in (" + mask + ")";
        try{
            return runner.query(sql, h, params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
