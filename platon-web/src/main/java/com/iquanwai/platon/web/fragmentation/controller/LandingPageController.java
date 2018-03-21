package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.flow.FlowService;
import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import com.iquanwai.platon.web.fragmentation.dto.LandingPageDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by 三十文
 */
@RestController
@RequestMapping("/rise/landing")
public class LandingPageController {

    @Autowired
    private FlowService flowService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadLandingPageData(UnionUser unionUser) {
        Integer unReadCount = messageService.unreadCount(unionUser.getId());
        List<LandingPageBanner> pageBanners = flowService.loadLandingPageBanners();
        List<ProblemsFlow> problemsFlows = flowService.loadProblemsFlow();
        List<LivesFlow> livesFlows = flowService.loadLivesFlow(3);
        List<ArticlesFlow> articlesFlows = flowService.loadArticlesFlow(3, false);
        List<ActivitiesFlow> activitiesFlows = flowService.loadActivitiesFlow(3);

        LandingPageDto dto = new LandingPageDto();
        dto.setIsBusinessMember(accountService.isBusinessRiseMember(unionUser.getId()));
        dto.setNotify(unReadCount != null && unReadCount > 0);
        dto.setPageBanners(pageBanners);
        dto.setProblemsFlows(problemsFlows);
        dto.setLivesFlows(livesFlows);
        dto.setArticlesFlows(articlesFlows);
        dto.setActivitiesFlows(activitiesFlows);
        return WebUtils.result(dto);
    }

    @RequestMapping("/load/shuffle/articles")
    public ResponseEntity<Map<String, Object>> loadShuffleArticles() {
        return WebUtils.result(flowService.loadArticlesFlow(3, true));
    }

    @RequestMapping("/load/lives")
    public ResponseEntity<Map<String, Object>> loadAllLives() {
        return WebUtils.result(flowService.loadLivesFlow());
    }

    @RequestMapping("/load/articles")
    public ResponseEntity<Map<String, Object>> loadAllArticles() {
        return WebUtils.result(flowService.loadArticlesFlow());
    }

    @RequestMapping("/load/activities")
    public ResponseEntity<Map<String, Object>> loadAllActivities() {
        return WebUtils.result(flowService.loadActivitiesFlow());
    }

}
