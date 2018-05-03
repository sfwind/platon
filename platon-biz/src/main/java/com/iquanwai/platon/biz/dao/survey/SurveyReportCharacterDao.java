package com.iquanwai.platon.biz.dao.survey;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.report.SurveyReportCharacter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class SurveyReportCharacterDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SurveyReportCharacter loadCharacterByHightAndLow(Integer highestId, Integer lowestId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveyReportCharacter where HighestId = ? and LowestId = ? and Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(SurveyReportCharacter.class), highestId, lowestId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


}
