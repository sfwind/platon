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

    /**
     * 根据直播 id 获取直播内容
     * @param liveId 直播 id
     */
    LivesFlow loadLiveFlowById(Integer profileId, Integer liveId);

    boolean orderLive(Integer orderId, Integer liveId);

    /**
     * 预约直播
     * @param orderId 预约人 id
     * @param promotionId 推广人 id
     * @param liveId 直播 id
     */
    boolean orderLive(Integer orderId, Integer promotionId, Integer liveId);
}
