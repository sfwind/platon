package com.iquanwai.platon.biz.dao.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemKnowledgeMap;
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
public class ProblemKnowledgeMapDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ProblemKnowledgeMap> loadKnowledges(int problemId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ProblemKnowledgeMap>> h = new BeanListHandler(ProblemKnowledgeMap.class);

        try {
            List<ProblemKnowledgeMap> problemKnowledgeMaps = run.query("SELECT * FROM ProblemKnowledgeMap where ProblemId=?", h,
                    problemId);
            return problemKnowledgeMaps;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
