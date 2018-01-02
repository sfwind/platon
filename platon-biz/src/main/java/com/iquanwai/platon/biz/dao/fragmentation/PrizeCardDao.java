package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.util.PrizeCardConstant;
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
     * 获得年度礼品卡
     *
     * @param profileId
     * @return
     */
    public List<PrizeCard> getAnnualPrizeCards(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PrizeCard where profileId = ? and Category = " + PrizeCardConstant.ANNUAL_PRIZE_CARD + " and Del = 0";
        ResultSetHandler<List<PrizeCard>> h = new BeanListHandler<>(PrizeCard.class);

        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    /**
     * 查询用户是否已经领取过礼品卡
     */
    public PrizeCard loadAnnualCardByReceiver(Integer receiverProfileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PrizeCard where ReceiverProfileId = ? and category = " + PrizeCardConstant.OFFLINE_PRIZE_CARD + " and Del = 0 limit 1";
        ResultSetHandler<PrizeCard> h = new BeanHandler<>(PrizeCard.class);

        try {
            return runner.query(sql, h, receiverProfileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


    /**
     * 插入年度礼品卡
     */
    public Integer insertAnnualPrizeCard(Integer profileId, String PrizeCardNo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " insert into PrizeCard(profileId,PrizeCardNo,Category) values (?,?," + PrizeCardConstant.ANNUAL_PRIZE_CARD + ")";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), profileId, PrizeCardNo);
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }


    /**
     * 领取预先生成的礼品卡
     *
     * @param id
     * @param profileId
     * @return
     */
    public Integer updatePreviewCard(Integer id, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PrizeCard set ReceiverProfileId = ?,used =1  where id = ? and ReceiverProfileId is null and del = 0";

        try {
            return runner.update(sql, profileId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    /**
     * 查询用户是否已经领取过礼品卡
     */
    public PrizeCard loadCardByCardNo(String cardNo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PrizeCard where PrizeCardNo = ? and category = " + PrizeCardConstant.OFFLINE_PRIZE_CARD + " and Del = 0 limit 1";
        ResultSetHandler<PrizeCard> h = new BeanHandler<>(PrizeCard.class);

        try {
            return runner.query(sql, h, cardNo);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 更新年度礼品卡
     *
     * @param prizeCardNo
     * @param openId
     * @param profileId
     * @return
     */
    public Integer updateAnnualCard(String prizeCardNo, String openId, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PrizeCard set ReceiverOpenId = ?,ReceiverProfileId = ?,Used = 1 where PrizeCardNo = ? and ReceiverProfileId is null and del = 0 ";

        try {
            return runner.update(sql, openId, profileId, prizeCardNo);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    /**
     * 查找领取者已经领取的年度礼品卡
     *
     * @param receiverProfileId
     * @return
     */
    public List<PrizeCard> loadReceiveAnnualCard(Integer receiverProfileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PrizeCard where ReceiverProfileId = ? and Category = " + PrizeCardConstant.ANNUAL_PRIZE_CARD + " and del = 0";
        ResultSetHandler<List<PrizeCard>> h = new BeanListHandler<>(PrizeCard.class);

        try {
            return runner.query(sql, h, receiverProfileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取礼品卡主人
     * @param cardNum
     * @return
     */
    public PrizeCard loadAnnualCardOwner(String cardNum){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from PrizeCard where PrizeCardNo = ? and and Category = " + PrizeCardConstant.ANNUAL_PRIZE_CARD+" and del = 0";
        ResultSetHandler<PrizeCard> h = new BeanHandler<>(PrizeCard.class);

        try {
            return runner.query(sql,h,cardNum);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }
}
