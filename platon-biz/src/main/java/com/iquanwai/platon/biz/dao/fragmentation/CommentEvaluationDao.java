package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CommentEvaluation;
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
 * Created by xfduan on 2017/8/2.
 */
@Repository
public class CommentEvaluationDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void initCommentEvaluation(Integer commentId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CommentEvaluation (CommentId) VALUES (?)";
        try {
            runner.insert(sql, new ScalarHandler<>(), commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateEvaluation(Integer commentId, Integer useful, String reason) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CommentEvaluation SET Useful = ?, Reason = ?, Evaluated = 1 WHERE CommentId = ?";
        try {
            runner.update(sql, useful, reason, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateEvaluation(Integer commentId, Integer useful) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CommentEvaluation SET Useful = ?, Evaluated = 1 WHERE CommentId = ?";
        try {
            runner.update(sql, useful, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<CommentEvaluation> loadUnEvaluatedCommentEvaluationBySubmitId(Integer submitId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CommentEvaluation WHERE SubmitId = ? AND Evaluated = 0";
        ResultSetHandler<List<CommentEvaluation>> h = new BeanListHandler<CommentEvaluation>(CommentEvaluation.class);
        try {
            return runner.query(sql, h, submitId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public CommentEvaluation loadByCommentId(Integer commentId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CommentEvaluation WHERE CommentId = ?";
        ResultSetHandler<CommentEvaluation> h = new BeanHandler<>(CommentEvaluation.class);
        try {
            return runner.query(sql, h, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
