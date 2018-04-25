package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.MemberTypeDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 2018/4/7.
 */
@Service
public class RiseMemberManagerImpl implements RiseMemberManager {
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberTypeDao memberTypeDao;

    private static Map<Integer, String> classMemberPrefixes = Maps.newHashMap();

    static {
        classMemberPrefixes.put(RiseMember.ELITE, "C");
        classMemberPrefixes.put(RiseMember.BUSINESS_THOUGHT, "S");
        classMemberPrefixes.put(RiseMember.CAMP, "M");
    }


    private Map<Integer, String> classNameMap = Maps.newHashMap();
    private Map<Integer, String> groupIdMap = Maps.newHashMap();


    @PostConstruct
    public void init() {
        memberTypeDao.loadAll(MemberType.class).forEach(item -> {
            classNameMap.put(item.getId(), "className_" + item.getId());
            groupIdMap.put(item.getId(), "groupId_" + item.getId());
        });
    }

    @Override
    public String classNameKey(Integer memberTypeId) {
        return classNameMap.get(memberTypeId);
    }

    @Override
    public String groupIdKey(Integer memberTypeId) {
        return groupIdMap.get(memberTypeId);
    }


    @Override
    public String memberPrefix(Integer memberType) {
        return classMemberPrefixes.get(memberType);
    }

    @Override
    public RiseMember coreBusinessSchoolMember(Integer profileId) {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.ELITE);
        members.add(RiseMember.HALF_ELITE);

        return getRiseMember(profileId, members);
    }

    private RiseMember getRiseMember(Integer profileId, List<Integer> members) {
        List<RiseMember> riseMembers = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, members);
        if (CollectionUtils.isEmpty(riseMembers)) {
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
        members.add(RiseMember.ELITE);
        members.add(RiseMember.HALF_ELITE);
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
        RiseMember riseMember = proMember(profileId);
        if (riseMember != null) {
            return DateUtils.afterDays(new Date(), dayCount).compareTo(riseMember.getExpireDate()) > 0;
        } else {
            return false;
        }
    }

    @Override
    public Boolean expiredRiseMember(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        // 合法会员数量
        Long valid = riseMembers.stream().filter(riseMember -> !riseMember.getExpired()).count();
        // 过期会员数量
        Long expired = riseMembers.stream().filter(RiseMember::getExpired).count();

        return valid == 0 && expired > 0;
    }

    @Override
    public String getMemberId(String openid) {
        String memberId = null;
        Profile profile = accountService.getProfile(openid);
        if (profile != null) {
            memberId = profile.getMemberId();
        }
        return memberId;
    }

    @Override
    public RiseMember getByMemberType(Integer profileId, Integer memberType) {
        return riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, Lists.newArrayList(memberType)).stream().findAny().orElse(null);
    }

    @Override
    public List<RiseMember> getAllValidElites() {
        List<Integer> members = Lists.newArrayList();
        members.add(RiseMember.ELITE);
        members.add(RiseMember.HALF_ELITE);
        members.add(RiseMember.BUSINESS_THOUGHT);

        return riseMemberDao.loadAllByMembers(members);
    }
}
