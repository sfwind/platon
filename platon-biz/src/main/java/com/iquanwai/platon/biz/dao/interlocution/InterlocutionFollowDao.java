package com.iquanwai.platon.biz.dao.interlocution;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionFollow;
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
 * Created by justin on 17/6/19.
 */
@Repository
public class InterlocutionFollowDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(InterlocutionFollow interlocutionFollow) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into InterlocutionFollow(QuestionId, Openid) " +
                "values(?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    interlocutionFollow.getQuestionId()
                    , interlocutionFollow.getOpenid());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<InterlocutionFollow> load(Integer questionId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<InterlocutionFollow>> h = new BeanListHandler<>(InterlocutionFollow.class);
        String sql = "SELECT * FROM InterlocutionFollow where QuestionId=? and Del=0";
        try {
            List<InterlocutionFollow> followList = run.query(sql, h, questionId);
            return followList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public InterlocutionFollow load(Integer questionId, String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<InterlocutionFollow> h = new BeanHandler<>(InterlocutionFollow.class);
        String sql = "SELECT * FROM InterlocutionFollow where QuestionId=? and Openid=?";
        try {
            InterlocutionFollow followList = run.query(sql, h, questionId, openId);
            return followList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void updateDel(Integer id, Integer del) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update InterlocutionFollow set Del=? where Id=?";
        try {

            runner.update(sql, del, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
