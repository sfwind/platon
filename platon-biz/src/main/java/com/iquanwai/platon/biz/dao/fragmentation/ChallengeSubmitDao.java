package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ChallengeSubmit;
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
 * Created by justin on 17/2/15.
 */
@Repository
public class ChallengeSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ChallengeSubmit challengeSubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ChallengeSubmit(ProfileId, ChallengeId, PlanId) " +
                "values(?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    challengeSubmit.getProfileId(),
                    challengeSubmit.getChallengeId(), challengeSubmit.getPlanId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询用户提交记录
     *
     * @param challengeId 小目标id
     * @param planId      计划id
     */
    public ChallengeSubmit load(Integer challengeId, Integer planId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ChallengeSubmit> h = new BeanHandler<>(ChallengeSubmit.class);
        String sql = "SELECT * FROM ChallengeSubmit where ProfileId=? and ChallengeId=? and PlanId=? and Del=0";
        try {
            return run.query(sql, h, profileId, challengeId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean firstAnswer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ChallengeSubmit set Content=?,Length=?,PublishTime=CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


    public boolean answer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ChallengeSubmit set Content=?, Length=? where Id=?";
        try {

            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


    public boolean updatePointStatus(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ChallengeSubmit set PointStatus=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void updateContent(Integer id, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ChallengeSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
