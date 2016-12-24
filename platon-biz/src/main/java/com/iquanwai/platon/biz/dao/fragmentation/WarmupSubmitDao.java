package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.WarmupSubmit;
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
 * Created by justin on 16/12/14.
 */
@Repository
public class WarmupSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(WarmupSubmit warmupSubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "insert into WarmupSubmit(Openid, QuestionId, PlanId, Content, IsRight, Score) " +
                "values(?,?,?,?,?,?)";
        try {
            asyncRun.update(sql, warmupSubmit.getOpenid(),warmupSubmit.getQuestionId(),
                    warmupSubmit.getPlanId(),warmupSubmit.getContent(),
                    warmupSubmit.getIsRight(), warmupSubmit.getScore());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<WarmupSubmit> getWarmupSubmit(int planId, List<Integer> questionIds){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupSubmit>> h = new BeanListHandler(WarmupSubmit.class);
        String questionMark = produceQuestionMark(questionIds.size());
        List<Object> objects = Lists.newArrayList();
        objects.add(planId);
        objects.addAll(questionIds);
        try {
            List<WarmupSubmit> submits = run.query("SELECT * FROM WarmupSubmit where PlanId=? and QuestionId in ("+questionMark+")",
                    h, objects.toArray());

            return submits;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
