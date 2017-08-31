package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.ProblemCollection;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemCollection;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class ProblemCollectionDao extends DBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 新增收藏
     */
    public Integer insert(Integer profileId, Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ProblemCollection (ProfileId, ProblemId) VALUES (?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), profileId, problemId);
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 删除收藏
     */
    public void delete(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ProblemCollection SET Del = 1 WHERE Id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ProblemCollection loadSingleProblemCollection(Integer profileId, Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ProblemCollection WHERE ProfileId = ? AND ProblemId = ? AND Del = 0";
        ResultSetHandler<ProblemCollection> h = new BeanHandler<>(ProblemCollection.class);
        try {
            return runner.query(sql, h, profileId, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 查看某人当前所有未删除收藏
     */
    public List<ProblemCollection> loadCollectionsByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ProblemCollection WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<ProblemCollection>> h = new BeanListHandler<>(ProblemCollection.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 将某一条记录恢复（该记录先前必然存在）
     */
    public Integer restoreCollection(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ProblemCollection SET Del = 0, CollectTime = ? WHERE Id = ?";
        try {
            return runner.update(sql, DateUtils.parseDateTimeToString(new Date()), id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
