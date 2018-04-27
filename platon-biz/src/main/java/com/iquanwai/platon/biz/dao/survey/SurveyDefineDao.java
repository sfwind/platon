package com.iquanwai.platon.biz.dao.survey;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyDefine;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class SurveyDefineDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SurveyDefine loadByIdAndType(Integer id, Integer type) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SurveyDefine WHERE DefineId = ? and Type = ?";
        try {
            return runner.query(sql, new BeanHandler<>(SurveyDefine.class), id, type);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
