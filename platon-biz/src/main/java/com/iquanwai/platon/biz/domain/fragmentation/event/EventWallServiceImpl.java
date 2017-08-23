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
        List<Integer> visibilities = Lists.newArrayList();
        ImprovementPlan plan = plans.stream()
                .filter(item -> (item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE)
                        && item.getProblemId().equals(eventWall.getVisibleProblemId())).findFirst().orElse(null);

        if(riseMember == null) {
            // 非会员（小课购买用户）
            visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER);
            visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER_AND_PROFESSIONAL);
            if (eventWall.getVisibleProblemId() != null && plan != null && eventWall.getVisibleProblemId().equals(plan.getProblemId())) {
                // 对小课用户做特殊处理，训练营用户同样的逻辑
                visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                if (eventWall.getType() == EventWall.OFFLINE) {
                    // 对于线下活动，权限等同于专业版
                    visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                } else {
                    // 对于其他，权限等于精英版
                    visibilities.add(EventWall.VisibleLevel.ELITE);
                }
            }
        } else {
            Integer memberTypeId = riseMember.getMemberTypeId();
            switch (memberTypeId) {
                case RiseMember.HALF:
                    // 专业版半年
                    visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                    visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                    visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER_AND_PROFESSIONAL);
                    break;
                case RiseMember.ANNUAL:
                    // 专业版一年
                    visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                    visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                    visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER_AND_PROFESSIONAL);
                    break;
                case RiseMember.ELITE:
                    // 精英版一年
                    visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                    visibilities.add(EventWall.VisibleLevel.ELITE);
                    break;
                case RiseMember.HALF_ELITE:
                    // 精英版半年
                    visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                    visibilities.add(EventWall.VisibleLevel.ELITE);
                    break;
                case RiseMember.CAMP:
                    // 训练营小课购买用户
                    visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER);
                    visibilities.add(EventWall.VisibleLevel.NOT_RISE_MEMBER_AND_PROFESSIONAL);
                    if (eventWall.getVisibleProblemId() != null && plan != null && eventWall.getVisibleProblemId().equals(plan.getProblemId())) {
                        // 对小课用户做特殊处理，训练营用户同样的逻辑
                        visibilities.add(EventWall.VisibleLevel.RISE_MEMBER);
                        if (eventWall.getType() == EventWall.OFFLINE) {
                            // 对于线下活动，权限等同于专业版
                            visibilities.add(EventWall.VisibleLevel.PROFESSIONAL);
                        } else {
                            // 对于其他，权限等于精英版
                            visibilities.add(EventWall.VisibleLevel.ELITE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return visibilities;
    }

}
