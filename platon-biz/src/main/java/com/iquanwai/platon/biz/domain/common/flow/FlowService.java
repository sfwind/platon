package com.iquanwai.platon.biz.domain.common.flow;

import com.iquanwai.platon.biz.po.flow.*;

import java.util.List;

/**
 * Created by 三十文
 */
public interface FlowService {
    /**
     * 获取首页banner
     */
    List<LandingPageBanner> loadLandingPageBanners();

    /**
     * 获取项目资源
     */
    List<ProgramsFlow> loadProgramsFlow(Integer profileId);

    /**
     * 获取直播资源
     */
    List<LivesFlow> loadLivesFlow(Integer profileId);

    /**
     * 获取直播资源
     */
    List<LivesFlow> loadLivesFlow(Integer profileId, Integer limit);

    /**
     * 获取文章资源
     */
    List<ArticlesFlow> loadArticlesFlow(Integer profileId);

    /**
     * 获取文章资源
     */
    List<ArticlesFlow> loadArticlesFlow(Integer profileId, Integer limit, Boolean shuffle);

    /**
     * 获取活动资源
     */
    List<ActivitiesFlow> loadActivitiesFlow(Integer profileId);

    /**
     * 获取活动资源
     */
    List<ActivitiesFlow> loadActivitiesFlow(Integer profileId, Integer limit);
}
