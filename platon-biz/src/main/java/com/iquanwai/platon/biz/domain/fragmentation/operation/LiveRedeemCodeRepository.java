package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.LiveRedeemCodeDao;
import com.iquanwai.platon.biz.po.common.LiveRedeemCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by nethunder on 2017/8/31.
 */
@Repository
public class LiveRedeemCodeRepository {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private LiveRedeemCodeDao liveRedeemCodeDao;

    public LiveRedeemCode useLiveRedeemCode(String lockKey, String live, Integer profileId) {
        // 先查询是否有优惠券
        LiveRedeemCode existCode = liveRedeemCodeDao.loadLiveRedeemCode(live, profileId);
        if (existCode == null) {
            return redisUtil.lock(lockKey, () -> {
                LiveRedeemCode liveRedeemCode = liveRedeemCodeDao.loadValidLiveRedeemCode(live);
                if (liveRedeemCode == null) {
                    logger.error("异常：live的兑换码耗尽");
                    return null;
                } else {
                    liveRedeemCodeDao.useRedeemCode(liveRedeemCode.getId(), profileId);
                    return liveRedeemCode;
                }
            });
        } else {
            return existCode;
        }
    }
}
