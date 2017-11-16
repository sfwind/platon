package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PrizeCardServiceImpl implements PrizeCardService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public PrizeCard loadNoOwnerPrizeCard(Integer profileId) {
        List<PrizeCard> prizeCards = Lists.newArrayList();

        PrizeCard personalPrizeCard = prizeCardDao.loadPersonalPrizeCard(profileId);
        if (personalPrizeCard != null) {
            prizeCards.add(prizeCardDao.loadPersonalPrizeCard(profileId));
        }

        if (prizeCards.size() == 0) {
            redisUtil.lock("prize:noOwner", lock -> {
                PrizeCard noOwnerPrizeCard = prizeCardDao.loadNoOwnerPrizeCard();
                if (noOwnerPrizeCard != null) {
                    prizeCards.add(noOwnerPrizeCard);
                }
            });
        }

        if (prizeCards.size() == 1) {
            PrizeCard prizeCard = prizeCards.get(0);
            prizeCard.setProfileId(profileId);

            // 用户领取完卡片之后，将对应的用户信息添加上去
            prizeCardDao.updateProfileId(profileId, prizeCard.getId());
            return prizeCard;
        } else {
            return null;
        }
    }

    @Override
    public boolean exchangePrizeCard(Integer profileId, Integer prizeCardId) {
        boolean exchangeResult = false;

        PrizeCard prizeCard = prizeCardDao.load(PrizeCard.class, prizeCardId);

        if (prizeCard.getUsed()) {
            exchangeResult = true;
        } else if (profileId.equals(prizeCard.getProfileId())) {
            // 确保兑换的是自己的卡片
            Coupon coupon = new Coupon();
            Profile profile = accountService.getProfile(profileId);
            coupon.setOpenId(profile.getOpenid());
            coupon.setProfileId(profileId);
            coupon.setAmount(100);
            coupon.setUsed(0);
            coupon.setExpiredDate(DateUtils.afterDays(new Date(), 8));
            coupon.setDescription("奖品卡");
            int couponInsertResult = couponDao.insertCoupon(coupon);

            if (couponInsertResult > 0) {
                exchangeResult = prizeCardDao.updateUsedInfo(prizeCardId) > 0;
            }
        }
        return exchangeResult;
    }

}
