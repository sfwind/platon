package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ChallengeSubmit;
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
public class ChallengeSubmitDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(ChallengeSubmit challengeSubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "insert into ChallengeSubmit(Openid, ChallengeId, PlanId, " +
                "SubmitUrl, ShortUrl) " +
                "values(?,?,?,?,?)";
        try {
            asyncRun.insert(sql, new ScalarHandler<>(),
                    challengeSubmit.getOpenid(), challengeSubmit.getChallengeId(),
                    challengeSubmit.getPlanId(),
                    challengeSubmit.getSubmitUrl(), challengeSubmit.getShortUrl());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }

    public ChallengeSubmit load(Integer challengeId, Integer planId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ChallengeSubmit> h = new BeanHandler(ChallengeSubmit.class);
        String sql = "SELECT * FROM ChallengeSubmit where Openid=? and ChallengeId=? and PlanId=?";
        try {
            ChallengeSubmit challengeSubmit = run.query(sql, h, openid, challengeId, planId);
            return challengeSubmit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public ChallengeSubmit load(String submitUrl){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ChallengeSubmit> h = new BeanHandler(ChallengeSubmit.class);
        String sql = "SELECT * FROM ChallengeSubmit where SubmitUrl=?";
        try {
            ChallengeSubmit challengeSubmit = run.query(sql, h, submitUrl);
            return challengeSubmit;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean answer(Integer id, String content){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ChallengeSubmit set Content=? where Id=?";
        try {

            runner.update(sql, content, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }

        return true;
    }
}
