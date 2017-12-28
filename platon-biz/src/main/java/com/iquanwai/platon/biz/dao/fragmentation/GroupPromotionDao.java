package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.GroupPromotion;
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
public class GroupPromotionDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(GroupPromotion groupPromotion) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO GroupPromotion (ProfileId, GroupCode, Leader) VALUES (?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    groupPromotion.getProfileId(),
                    groupPromotion.getGroupCode(),
                    groupPromotion.getLeader());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查看对应 groupCode 的所有成员
     */
    public List<GroupPromotion> loadByGroupCode(String groupCode) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM GroupPromotion WHERE GroupCode = ? AND Del = 0";
        ResultSetHandler<List<GroupPromotion>> h = new BeanListHandler<>(GroupPromotion.class);
        try {
            return runner.query(sql, h, groupCode);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public GroupPromotion loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM GroupPromotion WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<GroupPromotion> h = new BeanHandler<>(GroupPromotion.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
