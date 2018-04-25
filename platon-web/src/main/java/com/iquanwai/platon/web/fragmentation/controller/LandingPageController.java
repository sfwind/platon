package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.common.flow.FlowService;
import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.domain.common.member.RiseMemberTypeRepo;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.web.fragmentation.dto.ApplySuccessDto;
import com.iquanwai.platon.web.fragmentation.dto.LandingPageDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private RiseMemberManager riseMemberManager;
    @Autowired
    private ApplyService applyService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;

    @ApiOperation("获取着陆页所有信息")
    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadLandingPageData(UnionUser unionUser) {
        Integer unReadCount = messageService.unreadCount(unionUser.getId());
        Pair<Long, Integer> applicationPass = applyService.loadRemainTimeMemberTypeId(unionUser.getId());

        List<LandingPageBanner> pageBanners = flowService.loadLandingPageBanners();
        List<ProblemsFlow> problemsFlows = flowService.loadProblemsFlow(unionUser.getId());
        List<LivesFlow> livesFlows = flowService.loadLivesFlow(unionUser.getId(), 6);
        List<ArticlesFlow> articlesFlows = flowService.loadArticlesFlow(unionUser.getId(), 4, false);
        List<ActivitiesFlow> activitiesFlows = flowService.loadActivitiesFlow(unionUser.getId(), 4);

        LandingPageDto dto = new LandingPageDto();
        // TODO: 待验证
        List<RiseMember> riseMembers = riseMemberManager.businessSchoolMember(unionUser.getId());
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
        dto.setIsBusinessMember(CollectionUtils.isNotEmpty(riseMembers));
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

    @ApiOperation("获取单个直播内容")
    @RequestMapping(value = "/load/live", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLiveFlowById(UnionUser unionUser, @RequestParam("liveId") Integer liveId) {
        LivesFlow livesFlow = flowService.loadLiveFlowById(unionUser.getId(), liveId);
        if (livesFlow != null) {
            return WebUtils.result(livesFlow);
        } else {
            return WebUtils.error("未加载到当前直播内容");
        }
    }

    @ApiOperation("预约单个直播")
    @RequestMapping(value = "/order/live", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> orderLive(UnionUser unionUser, @RequestBody LivesOrder livesOrder) {
        boolean orderResult;
        Integer liveId = livesOrder.getLiveId();

        String promotionRiseId = livesOrder.getPromotionRiseId();
        Profile profile = accountService.getProfileByRiseId(promotionRiseId);
        if (profile != null) {
            Integer promotionId = profile.getId();
            orderResult = flowService.orderLive(unionUser.getId(), promotionId, liveId);
        } else {
            orderResult = flowService.orderLive(unionUser.getId(), liveId);
        }

        if (orderResult) {
            return WebUtils.success();
        } else {
            return WebUtils.error("直播预约失败，请稍后重试");
        }
    }

    public ResponseEntity<Map<String, Object>> loadSubscribeQrCode (UnionUser unionUser, ) {
        qrCodeService.loadQrBase64("");
    }

}
