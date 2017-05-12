package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.RiseMember;

/**
 * Created by justin on 17/5/11.
 */
public interface RiseMemberService {

    /**
     * 获取用户rise会员信息
     * @param openid 用户id
     * */
    RiseMember getRiseMember(String openid);
}
