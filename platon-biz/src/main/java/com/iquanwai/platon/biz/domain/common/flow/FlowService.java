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
    List<ProblemsFlow> loadProblemsFlow();

    List<LivesFlow> loadLivesFlow();

    List<ArticlesFlow> loadArticlesFlow();

    List<ActivitiesFlow> loadActivitiesFlow();
}
