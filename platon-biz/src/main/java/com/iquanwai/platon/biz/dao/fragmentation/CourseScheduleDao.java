package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CourseSchedule;
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
 * @version 2017-11-03
 */
@Repository
public class CourseScheduleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insertCourseSchedule(CourseSchedule schedule) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CourseSchedule(ProfileId, ProblemId, Year, Month) VALUES(?,?,?,?) ";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), schedule.getProfileId(), schedule.getProblemId(), schedule.getYear(), schedule.getMonth()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<CourseSchedule> getCertainMonthSchedule(Integer profileId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseSchedule WHERE ProfileId = ? AND Year = ? AND Month = ? AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<CourseSchedule>(CourseSchedule.class), profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public CourseSchedule getById(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseSchedule WHERE Id = ? ";
        try {
            return runner.query(sql, new BeanHandler<CourseSchedule>(CourseSchedule.class), id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer deleteById(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CourseSchedule SET Del = 1 WHERE Id = ? ";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer reopenById(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CourseSchedule SET Del = 0 WHERE Id = ? ";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<CourseSchedule> getAllScheduleByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseSchedule WHERE ProfileId = ? AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(CourseSchedule.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int updateProblemSchedule() {

    }
}
