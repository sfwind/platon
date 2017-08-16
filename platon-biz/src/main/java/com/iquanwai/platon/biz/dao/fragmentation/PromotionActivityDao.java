package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.PromotionActivity;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by xfduan on 2017/8/11.
 */
@Repository
public class PromotionActivityDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insertPromotionActivity(PromotionActivity promotionActivity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO PromotionActivity (ProfileId, Activity, Action) VALUES ( ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    promotionActivity.getProfileId(),
                    promotionActivity.getActivity(),
                    promotionActivity.getAction());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据用户 ProfileId 和 活动类型获取该用户在此次活动中的所有操作行为，同时可以区分该用户是否是本次活动中用户
     */
    public List<PromotionActivity> loadPromotionActivities(Integer profileId, String activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionActivity WHERE ProfileId = ? AND Activity = ?";
        ResultSetHandler<List<PromotionActivity>> h = new BeanListHandler<>(PromotionActivity.class);
        try {
            return runner.query(sql, h, profileId, activity);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 根据被推广的新人们的 ProfileIds 和 活动类型获取所有 PromotionActivity
     */
    public List<PromotionActivity> loadNewUsers(List<Integer> profileIds, String activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PromotionActivity WHERE ProfileId IN (" + produceQuestionMark(profileIds.size()) + ") " +
                "AND Activity = ?";
        ResultSetHandler<List<PromotionActivity>> h = new BeanListHandler<>(PromotionActivity.class);
        List<Object> objects = Lists.newArrayList();
        objects.addAll(profileIds);
        objects.add(activity);
        try {
            return runner.query(sql, h, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
