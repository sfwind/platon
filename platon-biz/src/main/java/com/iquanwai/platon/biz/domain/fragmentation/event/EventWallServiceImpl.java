package com.iquanwai.platon.biz.domain.fragmentation.event;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.EventWallDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.EventWall;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;
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
    private List<EventWallVisible> visibleList = Lists.newArrayList();

    @PostConstruct
    public void init() {
        visibleList.clear();
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.NO_RESTRICT, info -> true));
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.NO_PAY_NO_PLAN, info -> info.getRiseMember() == null && !info.getHasPlan()));
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.NOT_ELITE,
                info -> info.getRiseMember() == null ||
                        !(RiseMember.ELITE == info.getRiseMember().getMemberTypeId()
                                || RiseMember.HALF_ELITE == info.getRiseMember().getMemberTypeId())));
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.ELITE,
                info -> info.getRiseMember() != null &&
                        (RiseMember.ELITE == info.getRiseMember().getMemberTypeId()
                                || RiseMember.HALF_ELITE == info.getRiseMember().getMemberTypeId())));
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.CAMP,
                info -> info.getRiseMember() != null && info.getRiseMember().getMemberTypeId() == RiseMember.CAMP));
        visibleList.add(new EventWallVisible(EventWall.VisibleLevel.RISE_MEMBER,
                info -> info.getRiseMember() != null || info.getHasPlan()));
    }

    @Override
    public List<EventWall> getEventWall(Integer profileId) {
        return Lists.newArrayList();
    }

    private Boolean visibleCheck(EventWall eventWall, RiseMember riseMember, List<ImprovementPlan> plans) {
        // 删除的过滤掉
        if (eventWall.getDel()) {
            return false;
        }
        if (eventWall.getVisibility() == null || eventWall.getVisibility() == EventWall.VisibleLevel.NO_RESTRICT) {
            // 不对可见性做判断
            return true;
        } else {
            Boolean hasRunningPlan = plans.stream()
                    .anyMatch(item -> (item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE));
            UserInfo info = new UserInfo();
            info.setRiseMember(riseMember);
            info.setHasPlan(hasRunningPlan);
            // 对可见性做判断
            List<Integer> visibilities = visibleList.stream().filter(item -> item.getPredicate().test(info)).map(EventWallVisible::getVisible).collect(Collectors.toList());
            return visibilities.contains(eventWall.getVisibility());
        }
    }
}

@Data
class UserInfo {
    private RiseMember riseMember;
    private Boolean hasPlan;
}

@Data
@AllArgsConstructor
class EventWallVisible {
    private int visible;
    private Predicate<UserInfo> predicate;

}