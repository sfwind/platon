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
     * 判断是否是有效的专项课用户
     * @param profileId
     * @return
     */
    Boolean isValidCamp(Integer profileId);


    /**
     * 根据openid获取学号
     * @param openid
     * @return
     */
    String getMemberId(String openid);

    /**
     * 根据学号获取openid
     * @param memberId
     * @return
     */
    String getOpenid(String memberId);
}
