package com.iquanwai.platon.web.fragmentation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.message.ActivityMessageService;
import com.iquanwai.platon.biz.domain.common.message.ActivityMsg;
import com.iquanwai.platon.biz.domain.common.subscribe.SubscribeRouterService;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.*;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionAnswer;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author justin
 * @version 16/9/9
 */
@Controller
public class IndexController {
    @Autowired
    private OAuthService oAuthService;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //商学院按钮url
    private static final String INDEX_BUSINESS_SCHOOL_URL = "/rise/static/rise";
    //训练营按钮url
    private static final String INDEX_CAMP_URL = "/rise/static/camp";
    //关注页面
    private static final String SUBSCRIBE_URL = "/subscribe";
    //内测页面
    private static final String FORBID_URL = "/403.jsp";
    //训练营售卖页
    private static final String CAMP_SALE_URL = "/pay/camp";
    //商学院售卖页
    private static final String BUSINESS_SCHOOL_SALE_URL = "/pay/rise";
    //倒计时页面
    private static final String BUSINESS_COUNT_DOWN_URL = "/rise/static/business/count/down";
    //课程计划页面
    private static final String SCHEDULE_NOTICE = "/rise/static/course/schedule/start";
    //圈圈问答最近的页面
    private static final String QUANQUAN_ANSWER = "/rise/static/guest/inter/quan/answer?date=";
    //填写信息页面
    private static final String PROFILE_SUBMIT = "/rise/static/customer/profile?goRise=true";
    //申请成功页面
    private static final String APPLY_SUCCESS = "/pay/apply";
    //新学习页面
    private static final String NEW_SCHEDULE_PLAN = "/rise/static/course/schedule/plan";

    private static final String RISE_VIEW = "course";
    private static final String NOTE_VIEW = "note";

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

    @RequestMapping(value = "/rise/static/learn", method = RequestMethod.GET)
    public ModelAndView getLearnPage(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception {
        logger.info("点击学习页面");
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = null;
        Account account = null;
        if (accessToken != null) {
            openid = oAuthService.openId(accessToken);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                // 未关注
                response.sendRedirect(SUBSCRIBE_URL);
                return null;
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if (whiteListService.checkRiseMenuWhiteList(loginUser.getId())) {
            response.sendRedirect(INDEX_BUSINESS_SCHOOL_URL);
            return null;
        } else if (whiteListService.checkCampMenuWhiteList(loginUser.getId())) {
            response.sendRedirect(INDEX_CAMP_URL);
            return null;
        } else {
            List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(loginUser.getId());
            ModuleShow moduleShow = getModuleShow(loginUser, riseMembers);

            return courseView(request, loginUser, moduleShow, RISE_VIEW);
        }
    }

    @RequestMapping(value = "/rise/static/guest/note/**", method = RequestMethod.GET)
    public ModelAndView getGuestIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return courseView(request, null, new ModuleShow(), NOTE_VIEW);
    }

    @RequestMapping(value = {"/rise/static/guest/**"}, method = RequestMethod.GET)
    public ModelAndView getGuestInterIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("问题／答案页面,{},{}", request.getRequestURI(), request.getParameter("date"));
        return courseView(request, null, new ModuleShow(), RISE_VIEW);
    }

    @RequestMapping(value = {"/rise/static/note/**"}, method = RequestMethod.GET)
    public ModelAndView getBibleIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception {
        logger.info("note jsp");
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = null;
        Account account = null;
        if (accessToken != null) {
            openid = oAuthService.openId(accessToken);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                // 未关注
                response.sendRedirect(SUBSCRIBE_URL);
                logger.error("用户{}未关注", openid);
                return null;
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        //如果不在白名单中,直接403报错
        boolean result = whiteListService.isInBibleWhiteList(loginUser.getId());
        if (!result) {
            response.sendRedirect(FORBID_URL);
            return null;
        }

        return courseView(request, loginUser, new ModuleShow(), NOTE_VIEW);
    }

    @RequestMapping(value = "/rise/static/rise", method = RequestMethod.GET)
    public ModelAndView getRiseIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception {
        logger.info("点击商学院按钮");
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = null;
        Account account = null;
        if (accessToken != null) {
            openid = oAuthService.openId(accessToken);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                SubscribeRouterConfig subscribeRouterConfig = subscribeRouterService.loadUnSubscribeRouterConfig(request.getRequestURI());
                if (subscribeRouterConfig != null) {
                    // 未关注
                    response.sendRedirect(SUBSCRIBE_URL + "?scene=" + subscribeRouterConfig.getScene());
                    return null;
                } else {
                    response.sendRedirect(SUBSCRIBE_URL);
                    return null;
                }
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(loginUser.getId());
        ModuleShow moduleShow = getModuleShow(loginUser, riseMembers);

        //点击商学院,非年费用户和小课单买用户跳转售卖页
        if (request.getRequestURI().startsWith(INDEX_BUSINESS_SCHOOL_URL)) {
            //
            Boolean isElite = riseMembers.stream().anyMatch(item -> (!item.getExpired() && item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE));
            Profile profile = accountService.getProfile(loginUser.getId());
            Boolean modifyPlanSchedule = accountService.hasStatusId(loginUser.getId(), CustomerStatus.SCHEDULE_LESS);

            // 不是白名单
            if (!modifyPlanSchedule && isElite && isInfoComplete(profile)) {
                // 未填写信息的已购买商学院的 “新” 会员
                response.sendRedirect(PROFILE_SUBMIT);
                return null;
            } else if (whiteListService.isGoToCountDownNotice(loginUser.getId(), riseMembers)) {
                // 填完身份信息之后，开始学习日期未到
                response.sendRedirect(BUSINESS_COUNT_DOWN_URL);
                return null;
            } else if (whiteListService.isGoToScheduleNotice(loginUser.getId(), riseMembers)) {
                // 进入课程计划提示页面
                response.sendRedirect(SCHEDULE_NOTICE);
                return null;
            } else if (whiteListService.isGoToNewSchedulePlans(loginUser.getId(), riseMembers)) {
                // 进入新的学习页面
                response.sendRedirect(NEW_SCHEDULE_PLAN);
                return null;
            } else if (accountService.hasStatusId(loginUser.getId(), CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS)
                    && !whiteListService.checkRunningRiseMenuWhiteList(loginUser.getId())) {
                // 已经申请成功，有购买权限，非默认可购买的人(专业版)
                response.sendRedirect(APPLY_SUCCESS);
                return null;
            } else if (whiteListService.checkRiseMenuWhiteList(loginUser.getId())) {
                // 加载首屏广告信息
                activityMessageService.loginMsg(loginUser.getId());
            } else {
                response.sendRedirect(BUSINESS_SCHOOL_SALE_URL);
                return null;
            }
        }

        return courseView(request, loginUser, moduleShow, RISE_VIEW);
    }

    //个人信息是否完整
    private boolean isInfoComplete(Profile profile) {
        return profile.getAddress() == null ||
                (profile.getMobileNo() == null && profile.getWeixinId() == null) || profile.getIsFull() == 0;
    }

    @RequestMapping(value = "/rise/static/camp", method = RequestMethod.GET)
    public ModelAndView getCampIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception {
        logger.info("点击训练营按钮");

        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = null;
        Account account = null;
        if (accessToken != null) {
            openid = oAuthService.openId(accessToken);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                // 未关注
                response.sendRedirect(SUBSCRIBE_URL);
                return null;
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(loginUser.getId());
        ModuleShow moduleShow = getModuleShow(loginUser, riseMembers);

        //点击训练营,非小课训练营用户跳转售卖页
        if (request.getRequestURI().startsWith(INDEX_CAMP_URL)) {
            if (whiteListService.checkCampMenuWhiteList(loginUser.getId())) {
                activityMessageService.loginMsg(loginUser.getId());
            } else {
                response.sendRedirect(CAMP_SALE_URL);
                return null;
            }
        }

        return courseView(request, loginUser, moduleShow, RISE_VIEW);
    }

    @RequestMapping(value = {"/rise/static/**", "/forum/static/**"}, method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception {
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = null;
        Account account = null;
        if (accessToken != null) {
            openid = oAuthService.openId(accessToken);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                SubscribeRouterConfig subscribeRouterConfig = subscribeRouterService.loadUnSubscribeRouterConfig(request.getRequestURI());
                if (subscribeRouterConfig != null) {
                    // 未关注
                    response.sendRedirect(SUBSCRIBE_URL + "?scene=" + subscribeRouterConfig.getScene());
                    return null;
                } else {
                    response.sendRedirect(SUBSCRIBE_URL);
                    return null;
                }
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        // 加载首屏广告信息
        activityMessageService.loginMsg(loginUser.getId());

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(loginUser.getId());
        ModuleShow moduleShow = getModuleShow(loginUser, riseMembers);

        return courseView(request, loginUser, moduleShow, RISE_VIEW);
    }

    private ModuleShow getModuleShow(LoginUser loginUser, List<RiseMember> riseMembers) {
        // 菜单白名单 ,之后正式开放时，可以先在zk里关掉test，之后有时间在删掉这段代码，包括前后端,jsp
        ModuleShow moduleShow = new ModuleShow();
        Boolean showForum = true;
        if (ConfigUtils.isForumTest()) {
            // 论坛处于测试中,在白名单则显示，否则隐藏
            showForum = whiteListService.isInWhiteList(WhiteList.FORUM, loginUser.getId());
        }
        moduleShow.setShowForum(showForum);

        // 是否显示发现tab
        // 谁不显示：有课程计划表则不显示
        Boolean showExplore = whiteListService.isShowExploreTab(loginUser.getId(), riseMembers);
        moduleShow.setShowExplore(showExplore);
        return moduleShow;
    }

    @RequestMapping(value = "/rise/index/msg", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getIndexMsg(LoginUser loginUser) {
        if (loginUser == null) {
            logger.info("游客访问");
            return WebUtils.error(202, "游客访问");
        }
        ActivityMsg activityMsg = activityMessageService.getWelcomeMessage(loginUser.getId());

        return WebUtils.result(activityMsg);
    }

    private boolean checkAccessToken(HttpServletRequest request, String openid) {
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return true;
        }

        return !StringUtils.isEmpty(openid);
    }

    private ModelAndView courseView(HttpServletRequest request, LoginUser account, ModuleShow moduleShow, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String resourceUrl;
        String domainName = request.getHeader("Host-Test");
        switch (viewName) {
            case RISE_VIEW:
                resourceUrl = ConfigUtils.staticResourceUrl(domainName);
                break;
            case NOTE_VIEW:
                resourceUrl = ConfigUtils.staticNoteResourceUrl(domainName);
                break;
            default:
                resourceUrl = ConfigUtils.staticResourceUrl(domainName);
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

        Map<String, String> userParam = Maps.newHashMap();
        userParam.put("userName", account != null ? account.getWeixinName() : "");
        if (account != null && account.getHeadimgUrl() != null) {
            userParam.put("headImage", account.getHeadimgUrl().replace("http:", "https:"));
        }
        mav.addAllObjects(userParam);
        mav.addObject("showForum", moduleShow.getShowForum());
        mav.addObject("showExplore", moduleShow.getShowExplore());
        return mav;
    }
}

@Data
class ModuleShow {
    public ModuleShow() {
        this.showForum = false;
        this.showExplore = true;
    }

    private Boolean showForum;
    private Boolean showExplore;
}
