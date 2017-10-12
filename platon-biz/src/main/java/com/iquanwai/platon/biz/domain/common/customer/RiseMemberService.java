package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.RiseMember;

/**
 * Created by justin on 17/5/11.
 */
public interface RiseMemberService {

    /**
     * 获取用户rise会员信息
     * @param profileId 用户id
     * */
    RiseMember getRiseMember(Integer profileId);

    /**
     * 会员身份将在7日内过期
     */
    Boolean expiredRiseMemberInSevenDays(Integer profileId);

    /**
     * 会员身份过期
     */
    Boolean expiredRiseMember(Integer profileId);
}
