package com.iquanwai.platon.biz.domain.common.member;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.po.common.MemberType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/4/6.
 */
@Service
public class RiseMemberTypeRepoImpl implements RiseMemberTypeRepo {
    private static Map<Integer, MemberType> memberTypes = Maps.newConcurrentMap();

    @PostConstruct
    public void init(){
        memberTypes.put(1, new MemberType(1, 498D, "线上半年498"));
        memberTypes.put(2, new MemberType(2, 898D, "线上一年898"));
        memberTypes.put(3, new MemberType(3, 1980D, "线上一年+线下+学习报告1980"));
    }

    @Override
    public MemberType memberType(Integer memberTypeId) {
        return memberTypes.get(memberTypeId);
    }

    @Override
    public List<MemberType> loadAll() {
        return Lists.newArrayList(memberTypes.values());
    }
}