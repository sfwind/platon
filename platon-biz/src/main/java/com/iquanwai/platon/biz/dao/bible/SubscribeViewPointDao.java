package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.SubscribeViewPoint;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
@Repository
public class SubscribeViewPointDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<SubscribeViewPoint> loadAll(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeViewPoint WHERE ProfileId = ?";
        try {
            return runner.query(sql, new BeanListHandler<SubscribeViewPoint>(SubscribeViewPoint.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeViewPoint> load(Integer profileId, Date date, String tagsId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeViewPoint WHERE ProfileId = ? AND LearnDate = ? AND TagId in (?)";
        try {
            return runner.query(sql, new BeanListHandler<SubscribeViewPoint>(SubscribeViewPoint.class), profileId, DateUtils.parseDateToString(date), tagsId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeViewPoint> load(Integer profileId, Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeViewPoint WHERE ProfileId = ? AND LearnDate = ?";
        try {
            return runner.query(sql, new BeanListHandler<SubscribeViewPoint>(SubscribeViewPoint.class), profileId, DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<SubscribeViewPoint> load(Integer profileId, Integer tagId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SubscribeViewPoint WHERE ProfileId = ? AND TagId = ?";
        try {
            return runner.query(sql, new BeanListHandler<SubscribeViewPoint>(SubscribeViewPoint.class), profileId, tagId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer insert(SubscribeViewPoint subscribeViewPoint) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SubscribeViewPoint(ProfileId, TagId, Point, LearnDate) VALUES(?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), subscribeViewPoint.getProfileId(),
                    subscribeViewPoint.getTagId(), subscribeViewPoint.getPoint(), subscribeViewPoint.getLearnDate()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer update(Double point, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE SubscribeViewPoint SET Point = ? WHERE Id = ?";
        try {
            return runner.update(sql, point, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
