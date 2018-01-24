package com.iquanwai.platon.biz.dao.survey;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyChoice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * 问卷选项dao
 */
@Repository
public class SurveyChoiceDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<SurveyChoice> loadChoicesByQuestionCode(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<SurveyChoice>> h = new BeanListHandler<>(SurveyChoice.class);
        String mask = produceQuestionMark(codeList.size());
        List<Object> params = Lists.newArrayList();
        params.addAll(codeList);
        String sql = "SELECT * FROM SurveyChoice WHERE QuestionCode in (" + mask + ")";
        try {
            return runner.query(sql, h, params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
