package com.iquanwai.platon.biz.dao.daily;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.daily.DailyTalk;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class DailyTalkDao extends DBUtil{

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public DailyTalk loadByShowDate(String currentDate){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM DailyTalk WHERE ShowDate = ? AND DEL = 0 ";
        ResultSetHandler<DailyTalk> h = new BeanHandler<DailyTalk>(DailyTalk.class);

        try {
            return runner.query(sql,h,currentDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

}
