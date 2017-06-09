package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.dao.fragmentation.MemberTypeDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        RiseMember riseMember = riseMemberDao.validRiseMember(profileId);
        if (riseMember != null) {
            riseMember.setStartTime(DateUtils.parseDateToStringByCommon(riseMember.getAddTime()));
            riseMember.setEndTime(DateUtils.parseDateToStringByCommon(
                    DateUtils.beforeDays(riseMember.getExpireDate(), 1)));
            MemberType memberType = memberTypeDao.load(MemberType.class, riseMember.getMemberTypeId());
            riseMember.setName(memberType.getName());
        }
        return riseMember;
    }
}
