package com.iquanwai.platon.biz.domain.fragmentation.event;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.EventWallDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.ImprovementPlan;
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
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<EventWall> getEventWall(Integer profileId) {
        RiseMember riseMember = riseMemberDao.validRiseMember(profileId);
        List<ImprovementPlan> plans = improvementPlanDao.loadRunningPlan(profileId);

        List<EventWall> eventWalls = eventWallDao
                .loadAll(EventWall.class)
                .stream().filter((item) -> this.visibleCheck(item, riseMember, plans))
                .collect(Collectors.toList());
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

    private Boolean visibleCheck(EventWall eventWall, RiseMember riseMember, List<ImprovementPlan> plans) {
        // 删除的过滤掉
        if (eventWall.getDel()) {
            return false;
        }

        if (eventWall.getVisibility() == null) {
            // 不对可见性做判断
            return true;
        } else {
            // 对可见性做判断
            List<Integer> visibilities = calculateVisible(eventWall, riseMember, plans);
            return visibilities.contains(eventWall.getVisibility());
        }
    }

    private List<Integer> calculateVisible(EventWall eventWall, RiseMember riseMember, List<ImprovementPlan> plans) {
        // 计算可见性

        int NO_RESTRICT = 0; //不作限制
        int NOT_RISE_MEMBER = 1; //非付费
        int NOT_RISE_MEMBER_AND_PROFESSIONAL = 2; // 非精英
        int ELITE = 3; // 精英版
        int PROFESSIONAL = 4; // 非精英
        int RISE_MEMBER = 5; // 付费用户
        List<Integer> visibilities = Lists.newArrayList();
        Boolean hasRunningPlan = plans.stream()
                .anyMatch(item -> (item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE));
        // 不做限制的
        visibilities.add(EventWall.VisibleLevel.NO_RESTRICT);
        if (riseMember == null) {
            // 非精英
            visibilities.add(EventWall.VisibleLevel.NOT_ELITE);
            if (hasRunningPlan) {
                // 有正在进行的，当作付费
                visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
            } else {
                // 没有则非付费
                visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER);
            }
        } else {
            // 付费用户
            visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
            Integer memberTypeId = riseMember.getMemberTypeId();
            switch (memberTypeId) {
                case RiseMember.HALF:
                case RiseMember.ANNUAL:
                    // 专业版
                    visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                    visibilities.add(EventWall.VisibleLevel.NOT_ELITE);
                    break;
                case RiseMember.ELITE:
                case RiseMember.HALF_ELITE:
                    // 精英版
                    visibilities.add(EventWall.VisibleLevel.ELITE);
                    break;
                case RiseMember.CAMP:
                    visibilities.add(EventWall.VisibleLevel.NOT_ELITE);
                    visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                    break;
                default:
                    break;
            }
        }
        return visibilities;
    }

}
