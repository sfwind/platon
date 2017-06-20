package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.QuestionFollow;
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
public class QuestionFollowDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(QuestionFollow questionFollow) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into QuestionFollow(QuestionId, ProfileId) " +
                "values(?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    questionFollow.getProfileId(), questionFollow.getQuestionId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<QuestionFollow> load(Integer questionId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuestionFollow>> h = new BeanListHandler<>(QuestionFollow.class);
        String sql = "SELECT * FROM QuestionFollow where QuestionId=? and Del=0";
        try {
            List<QuestionFollow> followList = run.query(sql, h, questionId);
            return followList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public QuestionFollow load(Integer questionId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuestionFollow> h = new BeanHandler<>(QuestionFollow.class);
        String sql = "SELECT * FROM QuestionFollow where QuestionId=? and ProfileId=?";
        try {
            QuestionFollow followList = run.query(sql, h, questionId, profileId);
            return followList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void updateDel(Integer id, Integer del) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update QuestionFollow set Del=? where Id=?";
        try {

            runner.update(sql, del, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
