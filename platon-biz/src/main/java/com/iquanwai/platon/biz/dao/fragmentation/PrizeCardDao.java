package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PrizeCard;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

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


    /**
     * 根据profileId获得用户还未使用的礼品卡
     *
     * @param profileId
     * @return
     */
    public List<PrizeCard> getPrizeCardsByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<PrizeCard>> h = new BeanListHandler<>(PrizeCard.class);
        String sql = "select * from PrizeCard where ProfileId = ? and ReceiverOpenId is null and del = 0";

        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    /**
     * 设置礼品卡为领取状态
     * @param openId
     * @param id
     * @return
     */
    public Integer setCardReceived(String openId,Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PrizeCard set ReceiverOpenId = ? where id = ? and ReceiverOpenId is null";

        try {
           return runner.update(sql,openId,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

    /**
     * 设置礼品卡分享过
     * @param id
     */
    public void setCardShared(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PrizeCard set shared = 1 where id = ?";

        try {
            runner.update(sql,id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }

    }

}
