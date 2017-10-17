package com.iquanwai.platon.biz.dao.interlocution;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionDate;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

@Repository
public class InterlocutionDateDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public InterlocutionDate loadRecentDate() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from InterlocutionDate WHERE Del = 0 order by Id desc limit 1";
        try {
            return runner.query(sql, new BeanHandler<InterlocutionDate>(InterlocutionDate.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


    public InterlocutionDate loadDate(Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from InterlocutionDate WHERE Del = 0 and StartDate = ?";
        try {
            return runner.query(sql, new BeanHandler<InterlocutionDate>(InterlocutionDate.class), DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }



}
