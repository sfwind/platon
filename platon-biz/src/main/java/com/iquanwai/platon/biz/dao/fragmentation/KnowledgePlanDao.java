package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.KnowledgePlan;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by justin on 16/12/11.
 */
@Repository
public class KnowledgePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public KnowledgePlan getKnowledgePlan(Integer planId, Integer knowledgeId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from KnowledgePlan where PlanId=? and KnowledgeId=?";
        try {
            ResultSetHandler<KnowledgePlan> h = new BeanHandler(KnowledgePlan.class);
            return runner.query(sql, h, planId, knowledgeId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void insert(KnowledgePlan knowledgePlan){
        QueryRunner runner = new QueryRunner(getDataSource());
        try {
            String sql = "insert into KnowledgePlan(planId, knowledgeId, Appear) " +
                    "values(?,?,?)";

            runner.insert(sql, new ScalarHandler<>(),knowledgePlan.getPlanId(),
                    knowledgePlan.getKnowledgeId(), knowledgePlan.getAppear());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
