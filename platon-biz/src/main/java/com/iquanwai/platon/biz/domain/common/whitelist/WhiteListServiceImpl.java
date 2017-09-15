package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.dao.common.WhiteListDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/12/26.
 */
@Service
public class WhiteListServiceImpl implements WhiteListService {
    @Autowired
    private WhiteListDao whiteListDao;
    @Autowired
    private PromotionLevelDao promotionLevelDao;

    @Override
    public boolean isInWhiteList(String function, Integer profileId) {
        return whiteListDao.loadWhiteList(function, profileId) != null;
    }

    @Override
    public boolean isInBibleWhiteList(Integer profileId) {
        PromotionLevel level = promotionLevelDao.loadByProfileId(profileId, PromotionConstants.Activities.Bible);
        return level != null && level.getValid() == 1;
    }
}
