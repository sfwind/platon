package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/14.
 */
@Repository
public class WarmupSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(WarmupSubmit warmupSubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WarmupSubmit(Openid, ProfileId, QuestionId, PlanId, Content, IsRight, Score) " +
                "values(?,?,?,?,?,?,?)";
        try {
            runner.insert(sql, new ScalarHandler<>(), warmupSubmit.getOpenid(), warmupSubmit.getProfileId(),
                    warmupSubmit.getQuestionId(),
                    warmupSubmit.getPlanId(), warmupSubmit.getContent(),
                    warmupSubmit.getIsRight(), warmupSubmit.getScore());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<WarmupSubmit> getWarmupSubmit(int planId, List<Integer> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Lists.newArrayList();
        }
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupSubmit>> h = new BeanListHandler<>(WarmupSubmit.class);
        String questionMark = produceQuestionMark(questionIds.size());
        List<Object> objects = Lists.newArrayList();
        objects.add(planId);
        objects.addAll(questionIds);
        try {
            List<WarmupSubmit> submits = run.query("SELECT * FROM WarmupSubmit where PlanId=? and QuestionId in (" + questionMark + ") and Del=0",
                    h, objects.toArray());

            return submits;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public WarmupSubmit getWarmupSubmit(Integer profileId, Integer questionId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<WarmupSubmit> h = new BeanHandler<>(WarmupSubmit.class);
        try {
            WarmupSubmit submit = run.query("SELECT * FROM WarmupSubmit where ProfileId=? and QuestionId=? and Del=0",
                    h, profileId, questionId);

            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public WarmupSubmit getWarmupSubmit(Integer planId, Integer questionId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<WarmupSubmit> h = new BeanHandler<>(WarmupSubmit.class);
        try {
            WarmupSubmit submit = run.query("SELECT * FROM WarmupSubmit where PlanId=? and QuestionId=? and ProfileId=? and Del=0",
                    h, planId, questionId, profileId);

            return submit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
