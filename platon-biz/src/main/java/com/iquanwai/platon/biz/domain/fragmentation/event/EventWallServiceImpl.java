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
                .stream().filter((item) -> this.visibleCheck(item, riseMember,plans))
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

    private Boolean problemUserVisible(EventWall eventWall, List<ImprovementPlan> plans) {
        if (eventWall.getVisibleProblemId() == null) {
            // 不对小课用户做判断
            return false;
        } else {
            // 对小课用户做特殊处理
            if (eventWall.getType() == EventWall.OFFLINE) {
                // 先下活动，可见性为专业版
                eventWall.getVisibility()
            }
        }
    }

    private List<Integer> calculateVisible(EventWall eventWall, RiseMember riseMember, List<ImprovementPlan> plans) {
        // 计算可见性
        List<Integer> visibilities = Lists.newArrayList();
        if()
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
            if (eventWall.getVisibility() == EventWall.VisibleLevel.NOT_RISE_MEMBER) {
                // 非会员可见
                return riseMember == null || this.problemUserVisible(eventWall, plans);
            } else {
                if (eventWall.getVisibility() == EventWall.VisibleLevel.NOT_RISE_MEMBER_AND_PROFESSIONAL) {
                    // 非会员，专业版可见
                    return riseMember == null || riseMember.getMemberTypeId() == RiseMember.HALF || riseMember.getMemberTypeId() == RiseMember.ANNUAL;
                } else {
                    if (eventWall.getVisibility() == EventWall.VisibleLevel.ELITE) {
                        //精英版可见
                        Boolean eliteShow = riseMember != null
                                && (riseMember.getMemberTypeId() == RiseMember.ELITE
                                || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE);
                        if (!eliteShow) {
                            // 不是精英版可见性,查看是否购买过该小课
                            return false;
                        } else {
                            return true;
                        }

                    } else if (eventWall.getVisibility() == EventWall.VisibleLevel.PROFESSIONAL) {
                        // 专业版可见
                        return riseMember != null && (riseMember.getMemberTypeId() == RiseMember.HALF || riseMember.getMemberTypeId() == RiseMember.ANNUAL);
                    } else if (eventWall.getVisibility() == EventWall.VisibleLevel.RISE_MEMBER) {
                        // 会员可见，查看该用户是否购买过该小课，并且没有过期
                        Boolean isRiseMember = riseMember != null;
                        if (isRiseMember) {
                            return true;
                        } else {
                            // 不是会员，查看小课购买状态
                            // TODO 活动墙可见性
                            return false;
                        }
                    } else {
                        logger.error("未匹配到的可见性类型,{}", eventWall.getVisibility());
                        return false;
                    }
                }
            }
        }
    }
}
