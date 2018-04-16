package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CourseSchedule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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

    public int insertCourseSchedule(CourseSchedule schedule) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CourseSchedule(ProfileId, ProblemId, Year, Month, Type) VALUES(?, ?, ?, ?, ?) ";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(),
                    schedule.getProfileId(),
                    schedule.getProblemId(),
                    schedule.getYear(),
                    schedule.getMonth(),
                    schedule.getType()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void batchInsertCourseSchedule(List<CourseSchedule> schedules) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CourseSchedule(ProfileId, ProblemId, Category,Year, Month, Type, Recommend, Selected,MemberTypeId) " +
                " VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            Object[][] param = new Object[schedules.size()][];
            for (int i = 0; i < schedules.size(); i++) {
                CourseSchedule courseSchedule = schedules.get(i);
                param[i] = new Object[9];
                param[i][0] = courseSchedule.getProfileId();
                param[i][1] = courseSchedule.getProblemId();
                param[i][2] = courseSchedule.getCategory();
                param[i][3] = courseSchedule.getYear();
                param[i][4] = courseSchedule.getMonth();
                param[i][5] = courseSchedule.getType();
                param[i][6] = courseSchedule.getRecommend();
                param[i][7] = courseSchedule.getSelected();
                param[i][8] = courseSchedule.getMemberTypeId();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
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

    public List<CourseSchedule> loadAllMajorScheduleByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseSchedule WHERE ProfileId = ? AND Type = 1 AND Del = 0";
        ResultSetHandler<List<CourseSchedule>> h = new BeanListHandler<>(CourseSchedule.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int updateProblemSchedule(Integer profileId, Integer problemId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CourseSchedule SET Year = ?, Month = ? WHERE ProfileId = ? AND ProblemId = ? AND Del = 0";
        try {
            return runner.update(sql, year, month, profileId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int modifyScheduleYearMonth(Integer id, Integer year, Integer month, Integer selected) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CourseSchedule SET Year = ?, Month = ?, Selected = ? WHERE Id = ? AND Del = 0";
        try {
            return runner.update(sql, year, month, selected, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public CourseSchedule loadSingleCourseSchedule(Integer profileId, Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CourseSchedule WHERE ProfileId = ? AND ProblemId = ? AND Del = 0";
        ResultSetHandler<CourseSchedule> h = new BeanHandler<>(CourseSchedule.class);
        try {
            return runner.query(sql, h, profileId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public CourseSchedule loadOldestCoreCourseSchedule(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        // TODO bug点
        String sql = "SELECT * FROM CourseSchedule WHERE Type = 1 AND MemberTypeId = 3 AND Del = 0 AND ProfileId = ? ORDER BY Year ASC, Month ASC";
        ResultSetHandler<CourseSchedule> h = new BeanHandler<>(CourseSchedule.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public int updateSelected(Integer courseScheduleId, Integer selected) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE CourseSchedule SET Selected = ? WHERE Id = ?";
        try {
            return runner.update(sql, selected, courseScheduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
