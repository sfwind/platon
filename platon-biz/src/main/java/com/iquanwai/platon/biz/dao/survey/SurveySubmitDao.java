package com.iquanwai.platon.biz.dao.survey;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.survey.SurveySubmit;
import org.apache.commons.dbutils.QueryRunner;
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
public class SurveySubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(SurveySubmit surveySubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO fragmentCourse.SurveySubmit(Category, Version, ProfileId, Openid, SubmitTime) VALUE (?,?,?,?,CURRENT_TIMESTAMP)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), surveySubmit.getCategory(), surveySubmit.getVersion(),
                    surveySubmit.getProfileId(), surveySubmit.getOpenid()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
