package com.iquanwai.platon.biz.dao.survey;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * 问卷题目dao
 */
@Repository
public class SurveyQuestionDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<SurveyQuestion> loadQuestionByCategory(String category) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SurveyQuestion WHERE Category = ? AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(SurveyQuestion.class), category);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public SurveyQuestion loadOneQuestion(String category) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyQuestion where Category = ? and Del =0 limit 1";
        try {
            return runner.query(sql, new BeanHandler<>(SurveyQuestion.class), category);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
