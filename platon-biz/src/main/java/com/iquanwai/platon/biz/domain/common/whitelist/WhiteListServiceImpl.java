package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.dao.common.WhiteListDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 16/12/26.
 */
@Service
public class WhiteListServiceImpl implements WhiteListService {
    @Autowired
    private WhiteListDao whiteListDao;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AccountService accountService;

    @Override
    public boolean isInWhiteList(String function, Integer profileId) {
        return whiteListDao.loadWhiteList(function, profileId) != null;
    }

    @Override
    public boolean isInBibleWhiteList(Integer profileId) {
        PromotionLevel level = promotionLevelDao.loadByProfileId(profileId, PromotionConstants.Activities.Bible);
        return level != null && level.getValid() == 1;
    }

    /**
     * 点击商学院白名单
     */
    public boolean checkRiseMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        Long riseCount = riseMembers.stream().filter(riseMember ->
                riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE
        ).count();
        return riseCount.intValue() > 0;
    }

    /**
     * 点击小课训练营白名单
     */
    public boolean checkCampMenuWhiteList(Integer profileId) {

        return false;
    }

}
