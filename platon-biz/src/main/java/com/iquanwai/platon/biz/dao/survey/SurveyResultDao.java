package com.iquanwai.platon.biz.dao.survey;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * @author nethunder
 * 问卷提交记录
 */
@Repository
public class SurveyResultDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(SurveyResult surveyResult) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SurveyResult(Category, Version, ProfileId, Openid, SubmitTime,ReferSurveyId) VALUE (?,?,?,?,CURRENT_TIMESTAMP,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), surveyResult.getCategory(), surveyResult.getVersion(),
                    surveyResult.getProfileId(), surveyResult.getOpenid(), surveyResult.getReferSurveyId()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public SurveyResult loadByOpenid(String openId, String category) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyResult where Openid = ? AND Category = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(SurveyResult.class), openId, category);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
