package com.iquanwai.platon.biz.domain.common.flow;

import com.iquanwai.platon.biz.dao.common.ActivitiesFlowDao;
import com.iquanwai.platon.biz.dao.common.ArticlesFlowDao;
import com.iquanwai.platon.biz.dao.common.LivesFlowDao;
import com.iquanwai.platon.biz.dao.common.ProblemsFlowDao;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ArticlesFlow> loadArticlesFlow() {
        return articlesFlowDao.loadAllWithoutDel(ArticlesFlow.class);
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

}
