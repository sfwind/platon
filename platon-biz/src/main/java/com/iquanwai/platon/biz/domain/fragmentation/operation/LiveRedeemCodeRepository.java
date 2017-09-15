package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.LiveRedeemCodeDao;
import com.iquanwai.platon.biz.po.common.LiveRedeemCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

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

    public LiveRedeemCode useLiveRedeemCode(String live, Integer profileId) {
        Assert.notNull(live, "活动不能为空");
        Assert.notNull(profileId, "用户id不能为空");
        // 先查询是否有优惠券
        LiveRedeemCode existCode = liveRedeemCodeDao.loadLiveRedeemCode(live, profileId);
        if (existCode == null) {
            return redisUtil.lock("lock:live:code:" + live, () -> {
                // 这里重新查询，外面会过滤一层，这里再查一层
                LiveRedeemCode recheckExistCode = liveRedeemCodeDao.loadLiveRedeemCode(live, profileId);
                if (recheckExistCode == null) {
                    LiveRedeemCode liveRedeemCode = liveRedeemCodeDao.loadValidLiveRedeemCode(live);
                    if (liveRedeemCode == null) {
                        logger.error("异常：live的兑换码耗尽");
                        return null;
                    } else {
                        liveRedeemCodeDao.useRedeemCode(liveRedeemCode.getId(), profileId);
                        return liveRedeemCode;
                    }
                } else {
                    return recheckExistCode;
                }
            });
        } else {
            return existCode;
        }
    }
}
