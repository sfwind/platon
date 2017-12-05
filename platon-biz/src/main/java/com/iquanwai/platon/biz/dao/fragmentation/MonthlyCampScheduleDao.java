package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.MonthlyCampSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;


@Repository
public class MonthlyCampScheduleDao extends PracticeDBUtil{

    private Logger logger = LoggerFactory.getLogger(getClass());


    public MonthlyCampSchedule loadMonthlyScheduleByMonth(Integer month){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from MonthlyCampSchedule where month = ? order by id desc";
        ResultSetHandler<MonthlyCampSchedule> h = new BeanHandler<>(MonthlyCampSchedule.class);

        try {
            return  runner.query(sql,h,month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }

        return null;
    }

}
