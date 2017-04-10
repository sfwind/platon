package com.iquanwai.platon.biz.domain.common.member;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.MemberTypeDao;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/4/6.
 */
@Service
public class RiseMemberTypeRepoImpl implements RiseMemberTypeRepo {
    @Autowired
    private MemberTypeDao memberTypeDao;

    private static Map<Integer, MemberType> memberTypes = Maps.newConcurrentMap();
    private static Logger logger = LoggerFactory.getLogger(RiseMemberTypeRepoImpl.class);

    @PostConstruct
    public void init(){
        List<MemberType> types = memberTypeDao.loadAll(MemberType.class);
        types.forEach(item -> memberTypes.put(item.getId(), item));
        logger.info("RISE会员价格:{}", RiseMemberTypeRepoImpl.memberTypes);
    }

    @Override
    public MemberType memberType(Integer memberTypeId) {
        return memberTypes.get(memberTypeId)==null?null:memberTypes.get(memberTypeId).copy();
    }

    @Override
    public List<MemberType> loadAll() {
        List<MemberType> collect = memberTypes.values().stream().map(MemberType::copy).collect(Collectors.toList());
        collect.forEach(item->{
            item.setStartTime(DateUtils.parseDateToStringByCommon(new Date()));
            item.setEndTime(DateUtils.parseDateToStringByCommon(DateUtils.afterMonths(new Date(), item.getOpenMonth())));
        });
        return collect;
    }
}