package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.SubscribeUserTag;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 2017/9/13.
 */
@Repository
public class SubscribeUserTagDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SubscribeUserTag loadUserTag(Integer profileId, Integer tagId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeUserTag WHERE ProfileId = ? and TagId = ?";
        try {
            return runner.query(sql, new BeanHandler<SubscribeUserTag>(SubscribeUserTag.class), profileId, tagId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<SubscribeUserTag> loadAllUserFavorTags(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeUserTag WHERE ProfileId = ?";
        try {
            return runner.query(sql, new BeanListHandler<SubscribeUserTag>(SubscribeUserTag.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer insert(Integer profileId, Integer tagId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SubscribeUserTag(ProfileId, TagId) VALUES(?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), profileId, tagId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void choose(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE SubscribeUserTag SET Del = 0 WHERE Id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unchooseAll(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE SubscribeUserTag SET Del = 1 WHERE ProfileId = ?";
        try {
            runner.update(sql, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
