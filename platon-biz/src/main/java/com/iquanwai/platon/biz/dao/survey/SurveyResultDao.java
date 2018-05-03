package com.iquanwai.platon.biz.dao.survey;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * 问卷提交记录
 */
@Repository
public class SurveyResultDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(SurveyResult surveyResult) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SurveyResult(Category, Version, ProfileId, Openid, SubmitTime,ReferSurveyId, Level, ReportValid) VALUE (?,?,?,?,CURRENT_TIMESTAMP,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), surveyResult.getCategory(), surveyResult.getVersion(),
                    surveyResult.getProfileId(), surveyResult.getOpenid(), surveyResult.getReferSurveyId(), surveyResult.getLevel(), surveyResult.getReportValid()).intValue();
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

    public SurveyResult loadByOpenidAndReferId(String openId, Integer referId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyResult where Openid = ? AND ReferSurveyId = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(SurveyResult.class), openId, referId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<SurveyResult> loadReportValidByReferId(Integer referId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyResult where ReferSurveyId = ? and Del = 0 and ReportValid = 1";
        try {
            return runner.query(sql, new BeanListHandler<>(SurveyResult.class), referId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SurveyResult> loadByOpenIdAndCategory(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyResult where Openid = ? Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(SurveyResult.class), openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer generateReport(Integer submitId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SurveyResult set GeneratedReport = 1 where Id = ?";
        try {
            return runner.update(sql, submitId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<SurveyResult> loadByIdsAndCategory(List<Integer> ids, String category) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyResult where Id in (" + super.produceQuestionMark(ids.size()) + ") and Del = 0 and category = ?";
        try {
            List<Object> params = Lists.newArrayList(ids.toArray());
            params.add(category);
            return runner.query(sql, new BeanListHandler<>(SurveyResult.class), params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
