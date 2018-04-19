package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.common.message.ActivityMessageService;
import com.iquanwai.platon.biz.domain.common.message.ActivityMsg;
import com.iquanwai.platon.biz.domain.common.subscribe.SubscribeRouterService;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.user.UserInfoService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.SubscribeRouterConfig;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionAnswer;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author justin
 * @version 16/9/9
 */
@Controller
@ApiIgnore
public class IndexController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private InterlocutionService interlocutionService;
    @Autowired
    private SubscribeRouterService subscribeRouterService;
    @Autowired
    private ActivityMessageService activityMessageService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private ApplyService applyService;
    @Autowired
    private UserInfoService userInfoService;


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 商学院按钮url
    private static final String INDEX_BUSINESS_SCHOOL_URL = "/rise/static/rise";
    // 专项课按钮url
    private static final String INDEX_CAMP_URL = "/rise/static/camp";
    // 关注页面
    private static final String SUBSCRIBE_URL = "/subscribe";
    // 内测页面
    private static final String FORBID_URL = "/403.jsp";
    // 专项课售卖页
    private static final String CAMP_SALE_URL = "http://mp.weixin.qq.com/s?__biz=MzI1OTQ2OTY1OA==&mid=100000747&idx=1&sn=cffa80cc2c9303f102d40df574e3981c&chksm=6a793bde5d0eb2c89cf04523362a1ba1e1f49030e6bb6be38d167b4a6be9dd31a4e47465f6d2#rd";
    // private static final String CAMP_SALE_URL = "/pay/camp";
    // 商学院售卖页
    private static final String BUSINESS_SCHOOL_SALE_URL = "/pay/rise";
    // 课程计划页面
    private static final String SCHEDULE_NOTICE = "/rise/static/course/schedule/start";
    // 圈圈问答最近的页面
    private static final String QUANQUAN_ANSWER = "/rise/static/guest/inter/quan/answer?date=";
    // 填写信息页面
    private static final String PROFILE_SUBMIT = "/rise/static/customer/profile?goRise=true";
    // 申请核心课成功页面
    private static final String APPLY_CORE_SUCCESS = "/pay/apply?goodsId=3";
    // 申请思维课成功页面
    private static final String APPLY_THOUGHT_SUCCESS = "/pay/apply?goodsId=8";

    private static final String PROFILE_CAMP_SUBMIT = "/rise/static/customer/profile?goCamp=true";

    // 新学习页面
    private static final String NEW_SCHEDULE_PLAN = "/rise/static/course/schedule/plan";

    // 产品着陆页
    private static final String HOME_LANDING_PAGE = "/rise/static/home";

    private static final String RISE_VIEW = "course";

    @PostConstruct
    public void init() {
        logger.info("---------loadCertainDateArticles es.set.netty.runtime.available.processors:{}", System.getProperty("es.set.netty.runtime.available.processors"));
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> heartbeat() throws Exception {
        return WebUtils.success();
    }

    @RequestMapping(value = {"/rise/static/guest/recently/answer"}, method = RequestMethod.GET)
    public ModelAndView getGuestInterRecentlyIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InterlocutionAnswer answer = interlocutionService.loadRecentlyAnswer();
        String dateParam = DateUtils.parseDateToString(answer.getInterlocutionDate());
        logger.info("最近问题／答案页面：{}", dateParam);
        response.sendRedirect(QUANQUAN_ANSWER + dateParam);
        return null;
    }

    @RequestMapping(value = {"/rise/static/guest/**"}, method = RequestMethod.GET)
    public ModelAndView getGuestInterIndex(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        logger.info("问题／答案页面, {}, {}", request.getRequestURI(), request.getParameter("date"));
        return courseView(request, response, channel, new ModuleShow(), RISE_VIEW);
    }

    @RequestMapping(value = "/rise/static/learn", method = RequestMethod.GET)
    public ModelAndView getLearnPage(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser, @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            WebUtils.auth(request, response);
            return null;
        }

        if (whiteListService.checkRiseMenuWhiteList(unionUser.getId())) {
            response.sendRedirect(INDEX_BUSINESS_SCHOOL_URL);
            return null;
        } else if (whiteListService.checkCampMenuWhiteList(unionUser.getId())) {
            response.sendRedirect(INDEX_CAMP_URL);
            return null;
        } else {
            ModuleShow moduleShow = getModuleShow(unionUser);

            return courseView(request, response, channel, moduleShow, RISE_VIEW);
        }
    }

    /**
     * 主菜单：商学院
     */
    @RequestMapping(value = "/rise/static/rise", method = RequestMethod.GET)
    public ModelAndView getRiseIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser, @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            WebUtils.auth(request, response);
            return null;
        }

        if (notFollowCheck(request, response, unionUser)) {
            return null;
        }

        ModuleShow moduleShow = getModuleShow(unionUser);
        List<RiseMember> riseMembers = riseMemberManager.member(unionUser.getId());

        //是否是会员
        Boolean isMember = CollectionUtils.isNotEmpty(riseMembers);
        Profile profile = accountService.getProfile(unionUser.getId());
        UserInfo userInfo = userInfoService.loadByProfileId(unionUser.getId());

        if (!isMember) {
            response.sendRedirect(HOME_LANDING_PAGE);
            return null;
        }

        List<BusinessSchoolApplication> applyList = applyService.loadApplyList(unionUser.getId());
        boolean coreApplied = applyService.hasAvailableApply(applyList, Constants.Project.CORE_PROJECT);
        boolean thoughtApplied = applyService.hasAvailableApply(applyList, Constants.Project.BUSINESS_THOUGHT_PROJECT);

        if (isInfoUnComplete(profile, userInfo)) {
            // 未填写信息的已购买商学院的 “新” 会员
            response.sendRedirect(PROFILE_SUBMIT);
            return null;
        } else if (whiteListService.isGoToScheduleNotice(unionUser.getId(), riseMembers)) {
            // 进入课程计划提示页面
            response.sendRedirect(SCHEDULE_NOTICE);
            return null;
        } else if (whiteListService.isGoToNewSchedulePlans(unionUser.getId(), riseMembers)) {
            // 进入新的学习页面
            response.sendRedirect(NEW_SCHEDULE_PLAN);
            return null;
        } else if (coreApplied && !whiteListService.checkRunningRiseMenuWhiteList(unionUser.getId())) {
            // 已经申请成功，有购买权限，非默认可购买的人(专业版)
            response.sendRedirect(APPLY_CORE_SUCCESS);
            return null;
        } else if (thoughtApplied && !whiteListService.checkRunningRiseMenuWhiteList(unionUser.getId())) {
            // 已经申请成功，有购买权限，非默认可购买的人(专业版)
            response.sendRedirect(APPLY_THOUGHT_SUCCESS);
            return null;
        } else if (whiteListService.checkRiseMenuWhiteList(unionUser.getId())) {
            // 加载首屏广告信息
            activityMessageService.loginMsg(unionUser.getId());
        } else {
            response.sendRedirect(BUSINESS_SCHOOL_SALE_URL);
            return null;
        }

        return courseView(request, response, channel, moduleShow, RISE_VIEW);
    }

    /**
     * 主菜单：训练营
     */
    @RequestMapping(value = "/rise/static/camp", method = RequestMethod.GET)
    public ModelAndView getCampIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser, @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            WebUtils.auth(request, response);
            return null;
        }
        if (notFollowCheck(request, response, unionUser)) {
            return null;
        }

        ModuleShow moduleShow = getModuleShow(unionUser);

        // 当前身份如果是商学院会员，直接跳转着陆页
        List<RiseMember> riseMembers = riseMemberManager.member(unionUser.getId());
        //是否是会员
        Boolean isMember = CollectionUtils.isNotEmpty(riseMembers);
        if (isMember) {
            response.sendRedirect(HOME_LANDING_PAGE);
            return null;
        }

        //是否买过专业版
        Boolean isCampMember = riseMembers.stream().anyMatch(item -> (item.getMemberTypeId() == RiseMember.CAMP));
        Profile profile = accountService.getProfile(unionUser.getId());
        UserInfo userInfo = userInfoService.loadByProfileId(profile.getId());

        if (isCampMember && isPersonalInfoUnComplete(profile, userInfo)) {
            // 未填写信息的已购买专项课的 “新” 会员
            response.sendRedirect(PROFILE_CAMP_SUBMIT);
            return null;
        } else if (whiteListService.checkCampMenuWhiteList(unionUser.getId())) {
            // 加载首屏广告信息
            activityMessageService.loginMsg(unionUser.getId());
        } else if (whiteListService.isStillLearningCamp(unionUser.getId())) {
            return courseView(request, response, channel, moduleShow, RISE_VIEW);
        } else {
            response.sendRedirect(CAMP_SALE_URL);
            return null;
        }

        return courseView(request, response, channel, moduleShow, RISE_VIEW);
    }

    //所有信息是否完整
    private boolean isInfoUnComplete(Profile profile, UserInfo userInfo) {
        List<RiseMember> riseMembers = riseMemberManager.businessSchoolMember(profile.getId());
        if (CollectionUtils.isNotEmpty(riseMembers)) {
            return userInfo == null || userInfo.getAddress() == null || userInfo.getRealName() == null || userInfo.getReceiver() == null ||
                    (userInfo.getMobile() == null && profile.getWeixinId() == null);
        } else {
            RiseMember riseMember = riseMemberManager.campMember(profile.getId());
            if (riseMember != null) {
                return userInfo == null || (userInfo.getMobile() == null && profile.getWeixinId() == null);
            } else {
                return false;
            }
        }
    }

    //个人信息是否完整
    private boolean isPersonalInfoUnComplete(Profile profile, UserInfo userInfo) {
        return
                userInfo == null || (userInfo.getMobile() == null && profile.getWeixinId() == null);
    }

    /**
     * 检查是否关注，以及测试环境权限
     */
    private boolean notFollowCheck(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser) throws IOException {
        if (!accountService.checkIsSubscribe(unionUser.getOpenId(), unionUser.getUnionId())) {
            SubscribeRouterConfig subscribeRouterConfig = subscribeRouterService.loadUnSubscribeRouterConfig(request.getRequestURI());
            if (subscribeRouterConfig != null) {
                // 未关注
                response.sendRedirect(SUBSCRIBE_URL + "?scene=" + subscribeRouterConfig.getScene());
                return true;
            } else {
                response.sendRedirect(SUBSCRIBE_URL);
                return true;
            }
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, unionUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = {"/rise/static/**"}, method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser,
                                 @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            WebUtils.auth(request, response);
            return null;
        }

        if (notFollowCheck(request, response, unionUser)) {
            return null;
        }

        // 加载首屏广告信息
        activityMessageService.loginMsg(unionUser.getId());

        ModuleShow moduleShow = getModuleShow(unionUser);

        return courseView(request, response, channel, moduleShow, RISE_VIEW);
    }

    private ModuleShow getModuleShow(UnionUser unionUser) {
        // 菜单白名单 ,之后正式开放时，可以先在zk里关掉test，之后有时间在删掉这段代码，包括前后端,jsp
        ModuleShow moduleShow = new ModuleShow();

        // TODO: 待验证
        RiseMember riseMember = riseMemberManager.proMember(unionUser.getId());

        if (riseMember != null) {
            moduleShow.setShowExplore(true);
        } else {
            moduleShow.setShowExplore(false);
        }
        return moduleShow;
    }

    @RequestMapping(value = "/rise/index/msg", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getIndexMsg(UnionUser unionUser) {
        if (unionUser == null) {
            return WebUtils.error(202, "游客访问");
        }

        ActivityMsg activityMsg = activityMessageService.getWelcomeMessage(unionUser.getId());
        return WebUtils.result(activityMsg);
    }

    private ModelAndView courseView(HttpServletRequest request, HttpServletResponse response, String channel,
                                    ModuleShow moduleShow, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String resourceUrl;
        String domainName = request.getHeader("Host-Test");

        switch (viewName) {
            case RISE_VIEW:
                resourceUrl = ConfigUtils.staticResourceUrl(domainName);
                break;
            default:
                resourceUrl = ConfigUtils.staticResourceUrl(domainName);
        }

        // FIX BUG
        if (channel != null) {
            CookieUtils.removeCookie("_tm", channel, response);
        }

        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
            } else {
                mav.addObject("resource", resourceUrl);
            }
        } else {
            mav.addObject("resource", resourceUrl);
        }

        if (moduleShow != null) {
            mav.addObject("showExplore", moduleShow.getShowExplore());
        }
        mav.addObject("sensorsProject", ConfigUtils.getSensorsProject());
        return mav;
    }
}

@Data
class ModuleShow {
    public ModuleShow() {
        this.showExplore = true;
    }

    private Boolean showExplore;
}
