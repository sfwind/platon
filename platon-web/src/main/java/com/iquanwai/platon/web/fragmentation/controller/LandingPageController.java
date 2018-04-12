package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;
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
@Api(description = "获取登录页面的")
public class LandingPageController {

    @Autowired
    private FlowService flowService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerService customerService;

    @ApiOperation("获取着陆页所有信息")
    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadLandingPageData(UnionUser unionUser) {
        Integer unReadCount = messageService.unreadCount(unionUser.getId());
        Pair<Boolean, Long> applicationPass = customerService.isAlertApplicationPassMessage(unionUser.getId());
        List<LandingPageBanner> pageBanners = flowService.loadLandingPageBanners();
        List<ProblemsFlow> problemsFlows = flowService.loadProblemsFlow(unionUser.getId());
        List<LivesFlow> livesFlows = flowService.loadLivesFlow(unionUser.getId(), 6);
        List<ArticlesFlow> articlesFlows = flowService.loadArticlesFlow(unionUser.getId(), 4, false);
        List<ActivitiesFlow> activitiesFlows = flowService.loadActivitiesFlow(unionUser.getId(), 4);


        LandingPageDto dto = new LandingPageDto();
        dto.setIsBusinessMember(accountService.isBusinessRiseMember(unionUser.getId()));
        dto.setIsShowPassNotify(applicationPass.getLeft());
        dto.setRemainTime(applicationPass.getRight());
        dto.setNotify(unReadCount != null && unReadCount > 0);
        dto.setPageBanners(pageBanners);
        dto.setProblemsFlows(problemsFlows);
        dto.setLivesFlows(livesFlows);
        dto.setArticlesFlows(articlesFlows);
        dto.setActivitiesFlows(activitiesFlows);
        return WebUtils.result(dto);
    }

    @ApiOperation("获取随机乱序文章")
    @RequestMapping("/load/shuffle/articles")
    public ResponseEntity<Map<String, Object>> loadShuffleArticles(UnionUser unionUser) {
        return WebUtils.result(flowService.loadArticlesFlow(unionUser.getId(), 3, true));
    }

    @ApiOperation("获取所有直播内容")
    @RequestMapping("/load/lives")
    public ResponseEntity<Map<String, Object>> loadAllLives(UnionUser unionUser) {
        return WebUtils.result(flowService.loadLivesFlow(unionUser.getId()));
    }

    @ApiOperation("获取所有文章")
    @RequestMapping("/load/articles")
    public ResponseEntity<Map<String, Object>> loadAllArticles(UnionUser unionUser) {
        return WebUtils.result(flowService.loadArticlesFlow(unionUser.getId()));
    }

    @ApiOperation("获取活动列表")
    @RequestMapping("/load/activities")
    public ResponseEntity<Map<String, Object>> loadAllActivities(UnionUser unionUser) {
        return WebUtils.result(flowService.loadActivitiesFlow(unionUser.getId()));
    }

}
