package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.RiseMember;

import java.util.List;

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
     * 会员身份将在一定日期内过期
     */
    Boolean expiredRiseMemberInSomeDays(Integer profileId, Integer dayCount);

    /**
     * 会员身份过期
     */
    Boolean expiredRiseMember(Integer profileId);

    /**
     * 判断是否是有效的商学院会员
     * @param profileId
     * @return
     */
    Boolean isValidElite(Integer profileId);

    /**
     * 获取所有有效的商学院用户
     * @return
     */
    List<RiseMember> getValidElites();
}
