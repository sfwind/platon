package com.iquanwai.platon.biz.domain.common.flow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.*;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 三十文
 */
@Service
public class FlowServiceImpl implements FlowService {

    @Autowired
    private ProblemsFlowDao problemsFlowDao;
    @Autowired
    private LivesFlowDao livesFlowDao;
    @Autowired
    private ArticlesFlowDao articlesFlowDao;
    @Autowired
    private ActivitiesFlowDao activitiesFlowDao;
    @Autowired
    private LivesOrderDao livesOrderDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<LandingPageBanner> loadLandingPageBanners() {
        List<LandingPageBanner> pageBanners = Lists.newArrayList();

        String string = ConfigUtils.getLandingPageBanner();
        JSONArray bannerArray = JSONObject.parseArray(string);
        bannerArray.forEach(banner -> {
            JSONObject bannerJson = (JSONObject) banner;
            LandingPageBanner pageBanner = new LandingPageBanner();
            pageBanner.setImageUrl(bannerJson.getString("imageUrl"));
            pageBanner.setLinkUrl(bannerJson.getString("linkUrl"));
            pageBanners.add(pageBanner);
        });

        return pageBanners;
    }

    @Override
    public List<ProblemsFlow> loadProblemsFlow(Integer profileId) {
        List<ProblemsFlow> problemsFlows = problemsFlowDao.loadAllWithoutDel(ProblemsFlow.class);
        return problemsFlows;
    }

    @Override
    public List<LivesFlow> loadLivesFlow(Integer profileId) {
        List<LivesFlow> livesFlows = livesFlowDao.loadAllWithoutDel(LivesFlow.class);
        livesFlows = livesFlows.stream()
                .map(livesFlow -> {
                    if (livesFlow.getStartTime() != null) {
                        livesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(livesFlow.getStartTime()));
                    }
                    livesFlow.setVisibility(getVisibility(livesFlow, profileId));
                    return livesFlow;
                }).collect(Collectors.toList());
        return livesFlows;
    }

    @Override
    public List<LivesFlow> loadLivesFlow(Integer profileId, Integer limit) {
        List<LivesFlow> livesFlows = loadLivesFlow(profileId);
        livesFlows = livesFlows.stream().limit(limit).collect(Collectors.toList());
        return livesFlows;
    }

    @Override
    public List<ArticlesFlow> loadArticlesFlow(Integer profileId) {
        List<ArticlesFlow> articlesFlows = articlesFlowDao.loadAllWithoutDel(ArticlesFlow.class);
        return articlesFlows;
    }

    @Override
    public List<ArticlesFlow> loadArticlesFlow(Integer profileId, Integer limit, Boolean shuffle) {
        List<ArticlesFlow> articlesFlows = loadArticlesFlow(profileId);
        if (shuffle) {
            Collections.shuffle(articlesFlows);
        }
        articlesFlows = articlesFlows.stream().limit(limit).collect(Collectors.toList());
        return articlesFlows;
    }

    @Override
    public List<ActivitiesFlow> loadActivitiesFlow(Integer profileId) {
        List<ActivitiesFlow> activitiesFlows = activitiesFlowDao.loadAllWithoutDel(ActivitiesFlow.class);

        boolean isBusinessRiseMember = riseMemberManager.coreBusinessSchoolMember(profileId) != null || riseMemberManager.proMember(profileId) != null;

        activitiesFlows = activitiesFlows.stream()
                .map(activitiesFlow -> {
                    if (activitiesFlow.getStatus() == ActivitiesFlow.Status.PREPARE && activitiesFlow.getEndTime().compareTo(new Date()) < 0) {
                        activitiesFlowDao.downLine(activitiesFlow.getId());
                        activitiesFlow.setStatus(ActivitiesFlow.Status.CLOSED);
                    }
                    if (activitiesFlow.getStartTime() != null) {
                        activitiesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(activitiesFlow.getStartTime()));
                    }
                    if (activitiesFlow.getStatus() == ActivitiesFlow.Status.PREPARE) {
                        activitiesFlow.setTargetUrl(isBusinessRiseMember ? activitiesFlow.getVipSaleLinkUrl() : activitiesFlow.getGuestSaleLinkUrl());
                    } else if (activitiesFlow.getStatus() == ActivitiesFlow.Status.CLOSED) {
                        activitiesFlow.setTargetUrl(null);
                    } else if (activitiesFlow.getStatus() == ActivitiesFlow.Status.REVIEW) {
                        activitiesFlow.setTargetUrl(activitiesFlow.getLinkUrl());
                    } else {
                        activitiesFlow.setTargetUrl(null);
                    }
                    activitiesFlow.setVisibility(isBusinessRiseMember);
                    return activitiesFlow;
                })
                .sorted(Comparator.comparing(ActivitiesFlow::getStartTime).reversed())
                .collect(Collectors.toList());
        return activitiesFlows;
    }

    @Override
    public List<ActivitiesFlow> loadActivitiesFlow(Integer profileId, Integer limit) {
        List<ActivitiesFlow> activitiesFlows = loadActivitiesFlow(profileId);
        activitiesFlows = activitiesFlows.stream().limit(limit).collect(Collectors.toList());
        return activitiesFlows;
    }

    @Override
    public LivesFlow loadLiveFlowById(Integer profileId, Integer liveId) {
        LivesFlow livesFlow = livesFlowDao.load(LivesFlow.class, liveId);
        if (livesFlow != null) {
            if (livesFlow.getStartTime() != null) {
                livesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(livesFlow.getStartTime()));
            }
            LivesOrder livesOrder = livesOrderDao.loadByOrderIdAndLiveId(profileId, liveId);
            livesFlow.setIsOrdered(livesOrder != null);

            Profile profile = accountService.getProfile(profileId);
            livesFlow.setRiseId(profile.getRiseId());

            livesFlow.setVisibility(getVisibility(livesFlow, profileId));
        }
        return livesFlow;
    }

    @Override
    public boolean orderLive(Integer orderId, Integer liveId) {
        return orderLive(orderId, null, liveId);
    }

    @Override
    public boolean orderLive(Integer orderId, Integer promotionId, Integer liveId) {
        LivesOrder livesOrder = new LivesOrder();
        livesOrder.setOrderId(orderId);
        livesOrder.setPromotionId(promotionId);
        livesOrder.setLiveId(liveId);
        return livesOrderDao.insert(livesOrder) > 0;
    }

    private Boolean getVisibility(FlowData flowData, Integer profileId) {
        String authority = flowData.getAuthority();
        List<RiseMember> riseMembers = riseMemberManager.member(profileId);

        if (riseMembers.size() == 0) {
            // 当前身份为普通人
            if (authority != null) {
                char c = authority.charAt(authority.length() - 1);
                return "1".equals(String.valueOf(c));
            }
        } else {
            for (RiseMember riseMember : riseMembers) {
                Integer memberTypeId = 0;
                if (riseMember != null && riseMember.getMemberTypeId() != null) {
                    memberTypeId = riseMember.getMemberTypeId();
                }
                try {
                    char tagChar = authority.charAt(authority.length() - 1 - memberTypeId);
                    String tagValue = String.valueOf(tagChar);
                    return "1".equals(tagValue);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return false;
    }

}
