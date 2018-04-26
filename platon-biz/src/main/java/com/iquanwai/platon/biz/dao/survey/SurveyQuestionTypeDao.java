package com.iquanwai.platon.biz.dao.survey;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionType;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;

@Repository
public class SurveyQuestionTypeDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<SurveyQuestionType> loadQuestionTypes(List<String> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyQuestionType where QuestionCode in (" + this.produceQuestionMark(questionIds.size()) + ") AND Del = 0";
        try {
            runner.query(sql, new BeanListHandler<>(SurveyQuestionType.class), questionIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
