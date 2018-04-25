package com.iquanwai.platon.biz.domain.common.flow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.*;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.po.FlowData;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.flow.ActivitiesFlow;
import com.iquanwai.platon.biz.po.flow.ArticlesFlow;
import com.iquanwai.platon.biz.po.flow.LivesFlow;
import com.iquanwai.platon.biz.po.flow.ProgramsFlow;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by 三十文
 */
@Service
public class FlowServiceImpl implements FlowService {
    @Autowired
    private ProgramsFlowDao programsFlowDao;
    @Autowired
    private LivesFlowDao livesFlowDao;
    @Autowired
    private ArticlesFlowDao articlesFlowDao;
    @Autowired
    private ActivitiesFlowDao activitiesFlowDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private MemberTypeDao memberTypeDao;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 活动准备
     */
    private final static int ACTIVITY_PREPARE = 1;
    /**
     * 已关闭报名
     */
    private final static int ACTIVITY_CLOSED = 2;
    /**
     * 活动回顾
     */
    private final static int ACTIVITY_REVIEW = 3;
    /**
     * 倒计时
     */
    private final static int LIVE_COUNT_DOWN = 1;
    /**
     * 直播进行中
     */
    private final static int LIVE_ING = 2;
    /**
     * 回放
     */
    private final static int PLAY_BACK = 3;

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
    public List<ProgramsFlow> loadProgramsFlow(Integer profileId) {
        List<ProgramsFlow> programsFlowList = programsFlowDao.loadAllWithoutDel(ProgramsFlow.class);
        programsFlowList.forEach(programsFlow -> {
            MemberType memberType = memberTypeDao.load(MemberType.class, programsFlow.getMemberTypeId());
            if (memberType != null) {
                programsFlow.setInitPrice(memberType.getInitPrice());
                programsFlow.setPrice(memberType.getFee());
                try{
                    String requestUrl = "http://" + ConfigUtils.getInternalIp() + ":" + ConfigUtils.getInternalPort() +
                            "/internal/course/config/" + memberType.getId();
                    String body = restfulHelper.getPure(requestUrl);
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    int code = jsonObject.getInteger("code");
                    if(code == 200){
                        CourseConfig courseConfig = jsonObject.getObject("msg", CourseConfig.class);
                        programsFlow.setMonth(courseConfig.getSellingMonth());
                    }
                }catch (Exception e){
                    logger.error("获取售卖月份失败", e);
                }

                programsFlow.setOpen(memberType.getPurchaseSwitch());
                String numberStr = redisUtil.get("memberType:remain:" + memberType.getId());
                if (numberStr != null) {
                    programsFlow.setRemainNumber(Integer.valueOf(numberStr));
                }

                if (!memberType.getPurchaseSwitch()) {
                    programsFlow.setType(1);
                } else {
                    if (memberType.getInitPrice() != null) {
                        programsFlow.setType(2);
                    } else {
                        if (numberStr != null) {
                            programsFlow.setType(4);
                        } else {
                            programsFlow.setType(3);
                        }
                    }
                }
            }
        });

        return programsFlowList;
    }

    @Override
    public List<LivesFlow> loadLivesFlow(Integer profileId) {
        List<LivesFlow> livesFlows = livesFlowDao.loadAllWithoutDel(LivesFlow.class);
        livesFlows = livesFlows.stream()
                .map(livesFlow -> {
                    //开始时间转码
                    if (livesFlow.getStartTime() != null) {
                        livesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(livesFlow.getStartTime()));
                    }
                    //是否可见判断
                    livesFlow.setVisibility(getVisibility(livesFlow, profileId));
                    //状态判断
                    if (livesFlow.getStartTime() != null) {
                        // 开始时间晚于现在, 倒计时状态
                        if (livesFlow.getStartTime().after(new Date())) {
                            livesFlow.setStatus(LIVE_COUNT_DOWN);
                            // 直播开始两小时内, 进行中状态
                        } else if (DateUtils.afterHours(livesFlow.getStartTime(), 2).after(new Date())) {
                            livesFlow.setStatus(LIVE_ING);
                        } else {
                            // 直播开始两小时后, 回放状态
                            livesFlow.setStatus(PLAY_BACK);
                        }
                    }
                    return livesFlow;
                }).sorted((l1, l2) -> l2.getSequence() - l1.getSequence())
                .collect(Collectors.toList());
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
        articlesFlows.forEach(articlesFlow -> {
            List<String> tags = Lists.newArrayList();
            String tag = articlesFlow.getTag();
            //数据库用,分隔
            if (tag != null) {
                if (tag.contains(",")) {
                    String[] allTag = tag.split(",");
                    tags.addAll(Arrays.asList(allTag));
                } else {
                    tags.add(tag);
                }
            }
        });
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
                    if (activitiesFlow.getStatus() == ACTIVITY_PREPARE && activitiesFlow.getEndTime().compareTo(new Date()) < 0) {
                        activitiesFlowDao.downLine(activitiesFlow.getId());
                        activitiesFlow.setStatus(ACTIVITY_CLOSED);
                    }
                    if (activitiesFlow.getStartTime() != null) {
                        activitiesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(activitiesFlow.getStartTime()));
                    }
                    if (activitiesFlow.getStatus() == ACTIVITY_PREPARE) {
                        activitiesFlow.setTargetUrl(isBusinessRiseMember ? activitiesFlow.getVipSaleLinkUrl() : activitiesFlow.getGuestSaleLinkUrl());
                    } else if (activitiesFlow.getStatus() == ACTIVITY_CLOSED) {
                        activitiesFlow.setTargetUrl(null);
                    } else if (activitiesFlow.getStatus() == ACTIVITY_REVIEW) {
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
