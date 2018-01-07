package com.iquanwai.platon.biz.domain.common.customer;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.MemberTypeDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.util.DateUtils;
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
    private MemberTypeDao memberTypeDao;

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
    public Boolean expiredRiseMemberInSevenDays(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null && (riseMember.getMemberTypeId().equals(RiseMember.HALF)
                || riseMember.getMemberTypeId().equals(RiseMember.ANNUAL)
                || riseMember.getMemberTypeId().equals(RiseMember.ELITE)
                || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
            return DateUtils.afterDays(new Date(), 7).compareTo(riseMember.getExpireDate()) > 0;
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
     * @param profileId
     * @return
     */
    @Override
    public Boolean isValidElite(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if(riseMember!=null){
            Integer typeId = riseMember.getMemberTypeId();
            if(typeId.equals(RiseMember.ELITE) || typeId.equals(RiseMember.HALF_ELITE)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取有效的商学院用户
     * @return
     */
    @Override
    public List<RiseMember> getValidElites() {

        List<RiseMember> riseMembers = riseMemberDao.loadValidRiseMembers();
        List<RiseMember> validRiseMembers = Lists.newArrayList();
        riseMembers.forEach(riseMember -> {
            Integer typeId = riseMember.getMemberTypeId();
            if(typeId.equals(RiseMember.ELITE) || typeId.equals(RiseMember.HALF_ELITE)){
                validRiseMembers.add(riseMember);
            }
        });
        return validRiseMembers;
    }


}
