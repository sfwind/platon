package com.iquanwai.platon.biz.domain.common.flow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.ActivitiesFlowDao;
import com.iquanwai.platon.biz.dao.common.ArticlesFlowDao;
import com.iquanwai.platon.biz.dao.common.LivesFlowDao;
import com.iquanwai.platon.biz.dao.common.ProblemsFlowDao;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public List<ProblemsFlow> loadProblemsFlow() {
        return problemsFlowDao.loadAllWithoutDel(ProblemsFlow.class);
    }

    @Override
    public List<LivesFlow> loadLivesFlow() {
        List<LivesFlow> livesFlows = livesFlowDao.loadAllWithoutDel(LivesFlow.class);
        livesFlows.forEach(livesFlow -> {
            if (livesFlow.getStartTime() != null) {
                livesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(livesFlow.getStartTime()));
            }
        });
        return livesFlows;
    }

    @Override
    public List<LivesFlow> loadLivesFlow(Integer limit) {
        List<LivesFlow> livesFlows = loadLivesFlow();
        livesFlows = livesFlows.stream().limit(limit).collect(Collectors.toList());
        return livesFlows;
    }

    @Override
    public List<ArticlesFlow> loadArticlesFlow() {
        return articlesFlowDao.loadAllWithoutDel(ArticlesFlow.class);
    }

    @Override
    public List<ArticlesFlow> loadArticlesFlow(Integer limit, Boolean shuffle) {
        List<ArticlesFlow> articlesFlows = loadArticlesFlow();
        if (shuffle) {
            Collections.shuffle(articlesFlows);
        }
        articlesFlows = articlesFlows.stream().limit(limit).collect(Collectors.toList());
        return articlesFlows;
    }

    @Override
    public List<ActivitiesFlow> loadActivitiesFlow() {
        List<ActivitiesFlow> activitiesFlows = activitiesFlowDao.loadAllWithoutDel(ActivitiesFlow.class);
        activitiesFlows.forEach(activitiesFlow -> {
            if (activitiesFlow.getStartTime() != null) {
                activitiesFlow.setStartTimeStr(DateUtils.parseDateToFormat6(activitiesFlow.getStartTime()));
            }
        });
        return activitiesFlows;
    }

    @Override
    public List<ActivitiesFlow> loadActivitiesFlow(Integer limit) {
        List<ActivitiesFlow> activitiesFlows = loadActivitiesFlow();
        activitiesFlows = activitiesFlows.stream().limit(limit).collect(Collectors.toList());
        return activitiesFlows;
    }

}
