package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.iquanwai.platon.biz.po.RiseMember;

import java.util.List;

/**
 * Created by justin on 2018/4/7.
 */
public interface RiseMemberManager {

    /**
     * 商学院核心能力项目
     */
    RiseMember coreBusinessSchoolMember(Integer profileId);

    /**
     * 核心能力项目用户（商学院+专业版+专项课）
     */
    RiseMember coreBusinessSchoolUser(Integer profileId);

    /**
     * 专项课用户
     */
    RiseMember campMember(Integer profileId);

    /**
     * 商业思维类用户
     */
    RiseMember businessThought(Integer profileId);

    /**
     * 专业版用户
     */
    RiseMember proMember(Integer profileId);

    /**
     * 所有用户信息
     */
    List<RiseMember> member(Integer profileId);

    /**
     * 商学院用户信息
     */
    List<RiseMember> businessSchoolMember(Integer profileId);

    /**
     * 会员身份将在一定日期内过期
     */
    Boolean expiredRiseMemberInSomeDays(Integer profileId, Integer dayCount);

    /**
     * 会员身份过期
     */
    Boolean expiredRiseMember(Integer profileId);


    /**
     * 根据openid获取学号
     *
     * @param openid
     * @return
     */
    String getMemberId(String openid);

    RiseMember getByMemberType(Integer profileId, Integer memberType);
}
