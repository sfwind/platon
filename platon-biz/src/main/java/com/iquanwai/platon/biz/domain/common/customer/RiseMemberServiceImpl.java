package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.dao.fragmentation.MemberTypeDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/5/11.
 */
@Service
public class RiseMemberServiceImpl implements RiseMemberService {
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private MemberTypeDao memberTypeDao;
    @Autowired
    private AccountService accountService;

    @Override
    public RiseMember getRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getAddTime()));
            riseMember.setEndTime(DateUtils.parseDateToStringByCommon(
                    DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
            MemberType memberType = memberTypeDao.load(MemberType.class, riseMember.getMemberTypeId());
            riseMember.setName(memberType.getName());
        }
        return riseMember;
    }

    @Override
    public Boolean expiredRiseMemberInSomeDays(Integer profileId, Integer dayCount) {
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

    /**
     * 判断是否是商学院会员
     */
    @Override
    public Boolean isValidElite(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null) {
            Integer typeId = riseMember.getMemberTypeId();
            if (typeId.equals(RiseMember.ELITE) || typeId.equals(RiseMember.HALF_ELITE)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Boolean isValidCamp(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);

        if (riseMember != null) {
            Integer typeId = riseMember.getMemberTypeId();
            if (typeId.equals(RiseMember.CAMP)) {
                return true;
            }
        }
        return false;
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
    public String getOpenid(String memberId) {
        String openid = null;
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.loadByMemberId(memberId);
        if(CollectionUtils.isNotEmpty(riseClassMembers)){
            RiseClassMember riseClassMember = riseClassMembers.get(0);
            Profile profile = accountService.getProfile(riseClassMember.getProfileId());
            if (profile != null) {
                openid = profile.getOpenid();
            }
        }

        return openid;
    }


}
