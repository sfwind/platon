package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.common.flow.FlowService;
import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.domain.common.member.RiseMemberTypeRepo;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.flow.ActivitiesFlow;
import com.iquanwai.platon.biz.po.flow.ArticlesFlow;
import com.iquanwai.platon.biz.po.flow.LivesFlow;
import com.iquanwai.platon.biz.po.flow.ProgramsFlow;
import com.iquanwai.platon.web.fragmentation.dto.ApplySuccessDto;
import com.iquanwai.platon.web.fragmentation.dto.LandingPageDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by 三十文
 */
@RestController
@RequestMapping("/rise/landing")
@Api(description = "登录页面相关接口")
public class LandingPageController {

    @Autowired
    private FlowService flowService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ApplyService applyService;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;

    @ApiOperation(value = "获取着陆页所有信息", response = LandingPageDto.class)
    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLandingPageData(UnionUser unionUser) {
        Integer unReadCount = messageService.unreadCount(unionUser.getId());
        Pair<Long, Integer> applicationPass = applyService.loadRemainTimeMemberTypeId(unionUser.getId());

        List<LandingPageBanner> pageBanners = flowService.loadLandingPageBanners();
//        // 圈外课程
//        List<ProblemsFlow> problemsFlows = flowService.loadProblemsFlow(unionUser.getId());
        // 圈外项目
        List<ProgramsFlow> programsFlows = flowService.loadProgramsFlow(unionUser.getId());
        // 圈外直播
        List<LivesFlow> livesFlows = flowService.loadLivesFlow(unionUser.getId(), 6);
        // 圈外文章
        List<ArticlesFlow> articlesFlows = flowService.loadArticlesFlow(unionUser.getId(), 3, true);
        // 圈外活动
        List<ActivitiesFlow> activitiesFlows = flowService.loadActivitiesFlow(unionUser.getId(), 3);

        LandingPageDto dto = new LandingPageDto();

        ApplySuccessDto applySuccessDto = new ApplySuccessDto();
        applySuccessDto.setIsShowPassNotify(applicationPass.getLeft() > 0);
        applySuccessDto.setRemainTime(applicationPass.getLeft());
        applySuccessDto.setGoPayMemberTypeId(applicationPass.getRight());
        if (applicationPass.getRight() != null) {
            MemberType wanna = riseMemberTypeRepo.memberType(applicationPass.getRight());
            if (wanna != null) {
                applySuccessDto.setName(wanna.getDescription());
            }
        }

        dto.setApplySuccess(applySuccessDto);
        dto.setNotify(unReadCount != null && unReadCount > 0);
        dto.setPageBanners(pageBanners);
//        dto.setProblemsFlows(problemsFlows);
        dto.setProgramsFlows(programsFlows);
        dto.setLivesFlows(livesFlows);
        dto.setArticlesFlows(articlesFlows);
        dto.setActivitiesFlows(activitiesFlows);
        return WebUtils.result(dto);
    }

    @ApiOperation(value = "获取随机乱序文章", response = ArticlesFlow.class, responseContainer="List")
    @RequestMapping(value = "/load/shuffle/articles", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadShuffleArticles(UnionUser unionUser) {
        return WebUtils.result(flowService.loadArticlesFlow(unionUser.getId(), 3, true));
    }

    @ApiOperation(value = "获取所有直播内容", response = LivesFlow.class, responseContainer="List")
    @RequestMapping(value = "/load/lives", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadAllLives(UnionUser unionUser) {
        return WebUtils.result(flowService.loadLivesFlow(unionUser.getId()));
    }

    @ApiOperation(value = "获取所有文章", response = ArticlesFlow.class, responseContainer="List")
    @RequestMapping(value = "/load/articles", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadAllArticles(UnionUser unionUser) {
        return WebUtils.result(flowService.loadArticlesFlow(unionUser.getId()));
    }

    @ApiOperation(value = "获取活动列表", response = ActivitiesFlow.class, responseContainer="List")
    @RequestMapping(value = "/load/activities", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadAllActivities(UnionUser unionUser) {
        return WebUtils.result(flowService.loadActivitiesFlow(unionUser.getId()));
    }

}
