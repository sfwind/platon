package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PrizeCard;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class PrizeCardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public PrizeCard loadPersonalPrizeCard(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PrizeCard WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<PrizeCard> h = new BeanHandler<>(PrizeCard.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    // 获取数据库中尚未被领取的奖品卡片
    public PrizeCard loadNoOwnerPrizeCard() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM PrizeCard WHERE ProfileId IS NULL AND Del = 0 LIMIT 1";
        ResultSetHandler<PrizeCard> h = new BeanHandler<>(PrizeCard.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    // 更新礼品卡对应的用户信息
    public int updateProfileId(Integer profileId, Integer prizeCardId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE PrizeCard SET ProfileId = ? WHERE Id = ?";
        try {
            return runner.update(sql, profileId, prizeCardId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    // 用户领取之后，更新所有权信息
    public int updateUsedInfo(Integer prizeCardId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE PrizeCard SET Used = 1 WHERE Id = ?";
        try {
            return runner.update(sql, prizeCardId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
