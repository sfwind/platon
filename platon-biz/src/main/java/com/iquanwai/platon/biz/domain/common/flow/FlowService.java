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

    List<ProblemsFlow> loadProblemsFlow();

    List<LivesFlow> loadLivesFlow();

    /**
     * 获取直播数据
     * @param limit 获取近 limit 个数据
     * @return
     */
    List<LivesFlow> loadLivesFlow(Integer limit);

    List<ArticlesFlow> loadArticlesFlow();

    /**
     * 随机获取文章内容
     * @param limit 限制条数
     * @param shuffle 是否打乱次序
     */
    List<ArticlesFlow> loadArticlesFlow(Integer limit, Boolean shuffle);

    List<ActivitiesFlow> loadActivitiesFlow();

    /**
     * 获取活动数据
     */
    List<ActivitiesFlow> loadActivitiesFlow(Integer limit);
}
