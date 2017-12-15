package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.dao.common.WhiteListDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author justin
 * @version 16/12/26
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
        PromotionLevel level = promotionLevelDao.loadByProfileId(profileId, PromotionConstants.Activities.BIBLE);
        return level != null && level.getValid() == 1;
    }

    @Override
    public boolean isGoToCountDownNotice(Integer profileId, List<RiseMember> riseMembers) {
        return riseMembers.stream()
                .anyMatch(item -> (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE)
                        && new DateTime(item.getOpenDate()).isAfterNow() && !item.getExpired());
    }


    @Override
    public boolean isGoToScheduleNotice(Integer profileId, List<RiseMember> riseMembers) {
//        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        // 是商学院
        Boolean isElite = riseMembers.stream().anyMatch(item -> !item.getExpired() &&
                (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE));
        if (isElite) {
            Boolean scheduleWhiteList = accountService.hasStatusId(profileId, CustomerStatus.SCHEDULE_LESS);
            if (scheduleWhiteList) {
                // 老会员
                WhiteList whiteList = whiteListDao.loadWhiteList(WhiteList.SCHEDULE, profileId);
                // 老会员测试课程
                if (whiteList != null) {
                    Boolean hasCourseSchedule = CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
                    // 没有课程表
                    return !hasCourseSchedule;
                } else {
                    // 老会员不测试课程
                    return false;
                }
            } else {
                // 新会员
                Boolean hasCourseSchedule = CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
                // 没有课程表
                return !hasCourseSchedule;
            }
        } else {
            return false;
        }
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
    public boolean checkRunningRiseMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        Long riseCount = riseMembers.stream()
                .filter(riseMember -> !riseMember.getExpired())
                .filter(riseMember ->
                        // 商学院会员（半年、一年）、小课单买用户
                        riseMember.getMemberTypeId() == RiseMember.HALF
                                || riseMember.getMemberTypeId() == RiseMember.ANNUAL
                                || riseMember.getMemberTypeId() == RiseMember.ELITE
                                || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE
                                || riseMember.getMemberTypeId() == RiseMember.COURSE)
                .count();
        return riseCount.intValue() > 0;
    }

    @Override
    public boolean checkCampMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        Long risememberCount = riseMembers.stream().filter(riseMember ->
                // 半年/一年 精英版
                riseMember.getMemberTypeId() == RiseMember.ELITE ||
                        riseMember.getMemberTypeId() == RiseMember.HALF_ELITE
        ).count();
        // 如果转化成商学院 跳转训练营售卖页
        if (risememberCount > 0) {
            return false;
        }

        // 训练营
        Long campCount = riseMembers.stream()
                .filter(riseMember -> riseMember.getMemberTypeId() == RiseMember.CAMP
                        && riseMember.getOpenDate().compareTo(new DateTime().withTimeAtStartOfDay().toDate()) <= 0)
                .count();
        return campCount.intValue() > 0;
    }

    @Override
    public boolean isGoToNewSchedulePlans(Integer profileId, List<RiseMember> riseMembers) {
        // 是否精英版
        Boolean isElite = riseMembers.stream().anyMatch(item -> !item.getExpired() &&
                (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE));
        if (isElite) {
            // 是否老学员（不用开计划)
            Boolean scheduleWhiteList = accountService.hasStatusId(profileId, CustomerStatus.SCHEDULE_LESS);
            if (scheduleWhiteList) {
                // 老会员(但是要开计划，白名单)
                WhiteList whiteList = whiteListDao.loadWhiteList(WhiteList.SCHEDULE, profileId);
                if (whiteList != null) {
                    // 有课程表,而且要测试课程
                    return CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
                } else {
                    // 老会员不测试课程
                    return false;
                }
            } else {
                // 新会员&有课程表
                return CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
            }
        } else {
            // 不是精英版，肯定不会进去
            return false;
        }
    }

    @Override
    public Boolean isShowExploreTab(Integer profileId, List<RiseMember> riseMembers) {
        // 去新课程计划表则不显示
        return !this.isGoToNewSchedulePlans(profileId, riseMembers);
    }

    @Override
    public boolean isMonthlyCampStart(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember == null || riseMember.getMemberTypeId() != RiseMember.CAMP) {
            return false;
        } else {
            return riseMember.getOpenDate().compareTo(new Date()) >= 0;
        }
    }

}
