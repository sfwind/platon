package com.iquanwai.platon.biz.domain.common.member;


import com.iquanwai.platon.biz.po.common.MemberType;

import java.util.List;

/**
 * Created by nethunder on 2017/4/6.
 */
public interface RiseMemberTypeRepo {

    /**
     * 根据id获得memberType
     * @param memberTypeId 会员类型的id
     * @return 会员类型，如果该id无效，则返回null
     */
    MemberType memberType(Integer memberTypeId);

    /**
     * 获取所有会员类型
     */
    List<MemberType> loadAll();

}
