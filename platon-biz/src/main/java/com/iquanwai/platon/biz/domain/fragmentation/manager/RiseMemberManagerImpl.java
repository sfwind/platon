package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 2018/4/7.
 */
@Service
public class RiseMemberManagerImpl implements RiseMemberManager{
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private AccountService accountService;

    @Override
    public RiseMember coreBusinessSchoolMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.ELITE);
        members.add(RiseMember.HALF_ELITE);

        return getRiseMember(profileId, members);
    }

    private RiseMember getRiseMember(Integer profileId, List<Integer> members) {
        List<RiseMember> riseMembers = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
        if(CollectionUtils.isEmpty(riseMembers)){
            return null;
        }

        return riseMembers.get(0);
    }

    @Override
    public RiseMember coreBusinessSchoolUser(Integer profileId) {
        //TODO: 可能有多个身份
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);
        members.add(RiseMember.CAMP);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember campMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.CAMP);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember businessThought(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.BUSINESS_THOUGHT);

        return getRiseMember(profileId, members);
    }

    @Override
    public RiseMember proMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);

        return getRiseMember(profileId, members);
    }

    @Override
    public List<RiseMember> member(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF);
        members.add(RiseMember.ANNUAL);
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);
        members.add(RiseMember.CAMP);
        members.add(RiseMember.BUSINESS_THOUGHT);

        return riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
    }

    @Override
    public List<RiseMember> businessSchoolMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.ELITE);
        members.add(RiseMember.BUSINESS_THOUGHT);

        return riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
    }

    @Override
    public Boolean expiredRiseMemberInSomeDays(Integer profileId, Integer dayCount) {
        // TODO: justin
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null && (riseMember.getMemberTypeId().equals(RiseMember.HALF)
                || riseMember.getMemberTypeId().equals(RiseMember.ANNUAL)
                || riseMember.getMemberTypeId().equals(RiseMember.ELITE)
                || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
            return DateUtils.afterDays(new Date(), dayCount).compareTo(riseMember.getExpireDate()) > 0;
        } else {
            return false;
        }
    }

    @Override
    public Boolean expiredRiseMember(Integer profileId) {
        // TODO: justin
        RiseMember validRiseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (validRiseMember != null) {
            return false;
        }

        boolean tag = false;
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        for (RiseMember riseMember : riseMembers) {
            Integer memberTypeId = riseMember.getMemberTypeId();
            if ((memberTypeId.equals(RiseMember.HALF) || memberTypeId.equals(RiseMember.ANNUAL)
                    || memberTypeId.equals(RiseMember.ELITE) || memberTypeId.equals(RiseMember.HALF_ELITE))
                    && riseMember.getExpired()) {
                tag = true;
            }
        }
        return tag;
    }

    @Override
    public String getMemberId(String openid) {
        String memberId = null;
        Profile profile = accountService.getProfile(openid);
        if (profile != null) {
            RiseClassMember riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profile.getId());
            if(riseClassMember!=null){
                memberId = riseClassMember.getMemberId();
            }
        }
        return memberId;
    }

    @Override
    public List<RiseMember> getAllRiseMember(Integer profileId) {
        return riseMemberDao.loadRiseMembersByProfileId(profileId);
    }
}
