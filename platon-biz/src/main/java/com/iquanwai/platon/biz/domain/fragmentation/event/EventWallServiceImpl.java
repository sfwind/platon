package com.iquanwai.platon.biz.domain.fragmentation.event;

import com.iquanwai.platon.biz.dao.common.EventWallDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.EventWall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/6/5.
 */
@Service
public class EventWallServiceImpl implements EventWallService {
    @Autowired
    private EventWallDao eventWallDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<EventWall> getEventWall(String openid) {
        RiseMember riseMember = riseMemberDao.validRiseMember(openid);
        List<EventWall> eventWalls = eventWallDao
                .loadAll(EventWall.class).stream().filter(item -> {
                    if (item.getDel()) {
                        // 删除的过滤掉
                        return false;
                    }
                    if (item.getVisibility() == null) {
                        // 不对可见性做判断
                        return true;
                    } else {
                        if (item.getVisibility() == 1) {
                            // 非会员可见
                            return riseMember == null;
                        } else if (item.getVisibility() == 3) {
                            //精英版可见
                            return riseMember != null && riseMember.getMemberTypeId() == RiseMember.ELITE;
                        } else if (item.getVisibility() == 4) {
                            // 专业版可见
                            return riseMember != null && (riseMember.getMemberTypeId() == RiseMember.HALF || riseMember.getMemberTypeId() == RiseMember.ANNUAL);
                        } else if (item.getVisibility() == 5) {
                            // 会员可见
                            return riseMember != null;
                        } else {
                            logger.error("未匹配到的可见性类型,{}", item.getVisibility());
                            return false;
                        }
                    }
                }).collect(Collectors.toList());
        eventWalls.sort((o1, o2) -> {
            if (o1.getStartTime() == null) {
                return 1;
            } else if (o2.getStartTime() == null) {
                return -1;
            }
            return o2.getStartTime().before(o1.getStartTime()) ? -1 : 1;
        });
        return eventWalls;
    }
}
