package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.MonthlyCampSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class MonthlyCampScheduleDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public MonthlyCampSchedule loadByProblemId(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthlyCampSchedule WHERE ProblemId = ?";
        ResultSetHandler<MonthlyCampSchedule> h = new BeanHandler<>(MonthlyCampSchedule.class);
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<MonthlyCampSchedule> loadByMonth(Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MonthlyCampSchedule WHERE Month = ?";
        ResultSetHandler<List<MonthlyCampSchedule>> h = new BeanListHandler<>(MonthlyCampSchedule.class);
        try {
            return runner.query(sql, h, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

}
