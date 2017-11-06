package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.dao.common.WhiteListDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
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
    @Autowired
    private CourseScheduleDao courseScheduleDao;

    @Override
    public boolean isInWhiteList(String function, Integer profileId) {
        return whiteListDao.loadWhiteList(function, profileId) != null;
    }

    @Override
    public boolean isInBibleWhiteList(Integer profileId) {
        PromotionLevel level = promotionLevelDao.loadByProfileId(profileId, PromotionConstants.Activities.Bible);
        return level != null && level.getValid() == 1;
    }

    @Override
    public boolean isGoToCountDownNotice(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        return riseMembers.stream()
                .anyMatch(item -> (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE)
                        && new DateTime(item.getOpenDate()).isAfterNow());
    }

    @Override
    public boolean isGoToScheduleNotice(Integer profileId){
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        return riseMembers.stream()
                .anyMatch(item -> (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE)
                        && CollectionUtils.isEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId)));
    }


    @Override
    public boolean checkRiseMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        Long riseCount = riseMembers.stream().filter(riseMember ->
                // 商学院会员（半年、一年）、小课单买用户
                riseMember.getMemberTypeId() == RiseMember.HALF
                        || riseMember.getMemberTypeId() == RiseMember.ANNUAL
                        || riseMember.getMemberTypeId() == RiseMember.ELITE
                        || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE
                        || riseMember.getMemberTypeId() == RiseMember.COURSE
        ).count();
        return riseCount.intValue() > 0;
    }

    @Override
    public boolean checkCampMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        Long campCount = riseMembers.stream().filter(riseMember ->
                // 专业版会员（半年、一年）、小课训练营
                riseMember.getMemberTypeId() == RiseMember.CAMP
        ).count();
        return campCount.intValue() > 0;
    }

}
