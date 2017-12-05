package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.FullAttendanceReward;
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
 * Created by 三十文 on 2017/10/21
 */
@Repository
public class FullAttendanceRewardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(FullAttendanceReward fullAttendanceReward) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO FullAttendanceReward (ProfileId, ClassName, GroupId, MemberId, Year, Month, Amount) VALUES " +
                "( ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    fullAttendanceReward.getProfileId(),
                    fullAttendanceReward.getClassName(),
                    fullAttendanceReward.getGroupId(),
                    fullAttendanceReward.getMemberId(),
                    fullAttendanceReward.getYear(),
                    fullAttendanceReward.getMonth(),
                    fullAttendanceReward.getAmount());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<FullAttendanceReward> loadUnNotifiedByYearMonth(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM FullAttendanceReward WHERE Year = ? AND Month = ? AND Notified = 0 AND Del = 0";
        ResultSetHandler<List<FullAttendanceReward>> h = new BeanListHandler<>(FullAttendanceReward.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 查询单个用户对应的全勤奖学金
     * @param year
     * @param month
     * @param profileId
     * @return
     */
    public FullAttendanceReward loadUnNotifiedByYearMonthProfileId(Integer year,Integer month,Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from FullAttendanceReward  where Year = ? AND Month = ? AND ProfileId = ? AND Notified = 0 AND Del = 0";
        ResultSetHandler<FullAttendanceReward> h = new BeanHandler<>(FullAttendanceReward.class);
        try {
            return runner.query(sql,h,year,month,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    public FullAttendanceReward loadSingleByProfileId(Integer year, Integer month, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM FullAttendanceReward WHERE ProfileId = ? AND Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<FullAttendanceReward> h = new BeanHandler<>(FullAttendanceReward.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int updateNotify(Integer fullAttendanceRewardId, Integer notified) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE FullAttendanceReward SET Notified = ? WHERE Id = ?";
        try {
            return runner.update(sql, notified, fullAttendanceRewardId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
