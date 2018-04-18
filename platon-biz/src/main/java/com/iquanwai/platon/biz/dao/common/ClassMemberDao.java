package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ClassMember;
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
 * Created by 三十文
 */
@Repository
public class ClassMemberDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ClassMember classMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO fragmentCourse.ClassMember (ProfileId, ClassName, GroupId, MemberTypeId) VALUES (?, ?, ?, ?)";
        try {
            return runner.insert(sql, new ScalarHandler<>(),
                    classMember.getProfileId(),
                    classMember.getClassName(),
                    classMember.getGroupId(),
                    classMember.getMemberTypeId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<ClassMember> loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadByProfileIdAndMemberTypeId(Integer profileId, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND MemberTypeId = ? AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadByMemberTypeId(Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE MemberTypeId = ? AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ClassMember> loadByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    public ClassMember loadByProfileId(Integer profileId, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND MemberTypeId = ? AND Del = 0";
        ResultSetHandler<ClassMember> h = new BeanHandler<ClassMember>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public List<ClassMember> loadActiveByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Active = 1 AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ClassMember loadLatestByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


}
