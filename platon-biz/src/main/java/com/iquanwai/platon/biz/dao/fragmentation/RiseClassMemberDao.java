package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.RiseClassMember;
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

@Repository
public class RiseClassMemberDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseClassMember (ClassName, GroupId, MemberId, ProfileId, Active) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getMemberId(),
                    riseClassMember.getProfileId(),
                    riseClassMember.getActive());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseClassMember loadActiveRiseClassMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Active = 1 AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseClassMember> loadRiseClassMembersByYearMonth(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseClassMember loadRiseClassMemberByYearMonth(Integer year, Integer month, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Year = ? AND Month = ? AND ProfileId = ? AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, year, month, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
