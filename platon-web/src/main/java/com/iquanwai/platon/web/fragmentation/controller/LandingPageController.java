package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.flow.FlowService;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import com.iquanwai.platon.web.fragmentation.dto.LandingPageDto;
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

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadLandingPageData() {
        List<ProblemsFlow> problemsFlows = flowService.loadProblemsFlow();
        List<LivesFlow> livesFlows = flowService.loadLivesFlow();
        List<ArticlesFlow> articlesFlows = flowService.loadArticlesFlow();
        List<ActivitiesFlow> activitiesFlows = flowService.loadActivitiesFlow();

        LandingPageDto dto = new LandingPageDto();
        dto.setProblemsFlows(problemsFlows);
        dto.setLivesFlows(livesFlows);
        dto.setArticlesFlows(articlesFlows);
        dto.setActivitiesFlows(activitiesFlows);
        return WebUtils.result(dto);
    }

}
