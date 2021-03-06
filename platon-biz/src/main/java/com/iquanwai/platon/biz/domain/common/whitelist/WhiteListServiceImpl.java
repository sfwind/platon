package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.dao.common.WhiteListDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private RiseMemberManager riseMemberManager;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;

    @Override
    public boolean isInWhiteList(String function, Integer profileId) {
        return whiteListDao.loadWhiteList(function, profileId) != null;
    }

    @Override
    public boolean isGoToScheduleNotice(Integer profileId, List<RiseMember> riseMembers) {
        // 是商学院
        Boolean isElite = riseMembers.stream().anyMatch(item -> !item.getExpired() &&
                (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE));
        if (isElite) {
            // 商学院半年+一年
            Boolean hasCourseSchedule = CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
            // 没有课程表
            return !hasCourseSchedule;
        } else {
            return false;
        }
    }

    @Override
    public boolean checkRiseMenuWhiteList(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberManager.member(profileId);
        Long riseCount = riseMembers.stream().filter(riseMember ->
                        // 商学院会员（半年、一年）、课程单买用户
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
        List<RiseMember> riseMembers = riseMemberManager.member(profileId);
        Long riseCount = riseMembers.stream()
                .filter(riseMember -> !riseMember.getExpired())
                .filter(riseMember ->
                        // 商学院会员（半年、一年）、课程单买用户
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
        List<RiseMember> riseMembers = riseMemberManager.member(profileId);
        Long risememberCount = riseMembers.stream().filter(riseMember ->
                        // 半年/一年 精英版
                        riseMember.getMemberTypeId() == RiseMember.ELITE ||
                                riseMember.getMemberTypeId() == RiseMember.HALF_ELITE
        ).count();
        // 如果转化成商学院 跳转专项课售卖页
        if (risememberCount > 0) {
            return false;
        }

        // 专项课
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
            // 精英版是否有课程表
            return CollectionUtils.isNotEmpty(courseScheduleDao.getAllScheduleByProfileId(profileId));
        } else {
            // 不是精英版，肯定不会进去
            return false;
        }
    }

    @Override
    public boolean isStillLearningCamp(Integer profileId) {
//        Integer learningYear = ConfigUtils.getLearningYear();
//        Integer learningMonth = ConfigUtils.getLearningMonth();
//        RiseClassMember riseClassMember = riseClassMemberDao.loadSingleByProfileId(learningYear, learningMonth, profileId);
//        return riseClassMember != null;

        return false;
    }
}
