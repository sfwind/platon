package com.iquanwai.platon.biz.domain.common.flow;

import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;

import java.util.List;

/**
 * Created by 三十文
 */
public interface FlowService {
    List<LandingPageBanner> loadLandingPageBanners();

    /**
     * 获取课程资源
     */
    List<ProblemsFlow> loadProblemsFlow(Integer profileId);

    List<LivesFlow> loadLivesFlow(Integer profileId);

    List<LivesFlow> loadLivesFlow(Integer profileId, Integer limit);

    List<ArticlesFlow> loadArticlesFlow(Integer profileId);

    List<ArticlesFlow> loadArticlesFlow(Integer profileId, Integer limit, Boolean shuffle);

    List<ActivitiesFlow> loadActivitiesFlow(Integer profileId);

    List<ActivitiesFlow> loadActivitiesFlow(Integer profileId, Integer limit);
}
