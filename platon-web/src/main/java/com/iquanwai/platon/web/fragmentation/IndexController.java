package com.iquanwai.platon.web.fragmentation;

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
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserService;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import lombok.Data;
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
    // 商学院按钮url
    private static final String INDEX_BUSINESS_SCHOOL_URL = "/rise/static/rise";
    // 专项课按钮url
    private static final String INDEX_CAMP_URL = "/rise/static/camp";
    // 关注页面
    private static final String SUBSCRIBE_URL = "/subscribe";
    // 内测页面
    private static final String FORBID_URL = "/403.jsp";
    // 专项课售卖页
    private static final String CAMP_SALE_URL = "/pay/camp";
    // 专项课倒计时页面
    private static final String CAMP_COUNT_DOWN_URL = "/rise/static/camp/count/down";
    // 商学院售卖页
    private static final String BUSINESS_SCHOOL_SALE_URL = "/pay/rise";
    // 倒计时页面
    private static final String BUSINESS_COUNT_DOWN_URL = "/rise/static/business/count/down";
    // 课程计划页面
    private static final String SCHEDULE_NOTICE = "/rise/static/course/schedule/start";
    // 圈圈问答最近的页面
    private static final String QUANQUAN_ANSWER = "/rise/static/guest/inter/quan/answer?date=";
    // 填写信息页面
    private static final String PROFILE_SUBMIT = "/rise/static/customer/profile?goRise=true";
    private static final String PROFILE_CAMP_SUBMIT = "/rise/static/customer/profile?goRise=true&goCamp=true";
    // 申请成功页面
    private static final String APPLY_SUCCESS = "/pay/apply";
    // 新学习页面
    private static final String NEW_SCHEDULE_PLAN = "/rise/static/course/schedule/plan";

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

    @RequestMapping(value = "/rise/static/learn", method = RequestMethod.GET)
    public ModelAndView getLearnPage(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser,
                                     @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            logger.info("unionUser 为空");
            WebUtils.auth(request, response);
            return null;
        }

        String state = CookieUtils.getCookie(request, OAuthService.MOBILE_STATE_COOKIE_NAME);
        String openid;
        Account account;
        if (state != null) {
            openid = oAuthService.openId(state);
            try {
                account = accountService.getAccount(openid, false);
                logger.info("account:{}", account);
            } catch (NotFollowingException e) {
                // 未关注
                response.sendRedirect(SUBSCRIBE_URL);
                return null;
            }
        }

        if (whiteListService.checkRiseMenuWhiteList(unionUser.getId())) {
            response.sendRedirect(INDEX_BUSINESS_SCHOOL_URL);
            return null;
        } else if (whiteListService.checkCampMenuWhiteList(unionUser.getId())) {
            response.sendRedirect(INDEX_CAMP_URL);
            return null;
        } else {
            List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(unionUser.getId());
            ModuleShow moduleShow = getModuleShow(unionUser, riseMembers);

            return courseView(request, response, channel, moduleShow, RISE_VIEW);
        }
    }

    @RequestMapping(value = {"/rise/static/guest/**"}, method = RequestMethod.GET)
    public ModelAndView getGuestInterIndex(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        logger.info("问题／答案页面, {}, {}", request.getRequestURI(), request.getParameter("date"));
        return courseView(request, response, channel, new ModuleShow(), RISE_VIEW);
    }

    /**
     * 主菜单：商学院
     */
    @RequestMapping(value = "/rise/static/rise", method = RequestMethod.GET)
    public ModelAndView getRiseIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser,
                                     @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        logger.info("点击商学院按钮");
        if (unionUser == null) {
            logger.info("unionUser 为空");
            WebUtils.auth(request, response);
            return null;
        }
        logger.info("unionUser 不为空");

        String accessToken = CookieUtils.getCookie(request, UnionUserService.MOBILE_STATE_COOKIE_NAME);
        String openid;
        Account account;
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

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, unionUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(unionUser.getId());
        ModuleShow moduleShow = getModuleShow(unionUser, riseMembers);

        //是否买过或曾经买过 商学院/专业版
        Boolean isMember = riseMembers.stream().anyMatch(item -> (item.getMemberTypeId() == RiseMember.ELITE ||
                item.getMemberTypeId() == RiseMember.HALF_ELITE) || item.getMemberTypeId() == RiseMember.ANNUAL ||
                item.getMemberTypeId() == RiseMember.HALF);
        Profile profile = accountService.getProfile(unionUser.getId());

        if (isMember && isInfoInComplete(profile)) {
            // 未填写信息的已购买商学院的 “新” 会员
            response.sendRedirect(PROFILE_SUBMIT);
            return null;
        } else if (whiteListService.isGoToCountDownNotice(unionUser.getId(), riseMembers)) {
            // 填完身份信息之后，开始学习日期未到
            response.sendRedirect(BUSINESS_COUNT_DOWN_URL);
            return null;
        } else if (whiteListService.isGoToScheduleNotice(unionUser.getId(), riseMembers)) {
            // 进入课程计划提示页面
            response.sendRedirect(SCHEDULE_NOTICE);
            return null;
        } else if (whiteListService.isGoToNewSchedulePlans(unionUser.getId(), riseMembers)) {
            // 进入新的学习页面
            response.sendRedirect(NEW_SCHEDULE_PLAN);
            return null;
        } else if (accountService.hasStatusId(unionUser.getId(), CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS)
                && !whiteListService.checkRunningRiseMenuWhiteList(unionUser.getId())) {
            // 已经申请成功，有购买权限，非默认可购买的人(专业版)
            response.sendRedirect(APPLY_SUCCESS);
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

    //所有信息是否完整
    private boolean isInfoInComplete(Profile profile) {
        return profile.getAddress() == null || profile.getRealName() == null || profile.getReceiver() == null ||
                (profile.getMobileNo() == null && profile.getWeixinId() == null) || profile.getIsFull() == 0;
    }

    //个人信息是否完整
    private boolean isPersonalInfoInComplete(Profile profile) {
        return profile.getRealName() == null ||
                (profile.getMobileNo() == null && profile.getWeixinId() == null) || profile.getIsFull() == 0;
    }

    /**
     * 主菜单：训练营
     */
    @RequestMapping(value = "/rise/static/camp", method = RequestMethod.GET)
    public ModelAndView getCampIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser,
                                     @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        if (unionUser == null) {
            WebUtils.auth(request, response);
            return null;
        }

        String accessToken = CookieUtils.getCookie(request, OAuthService.MOBILE_STATE_COOKIE_NAME);
        String openid;
        Account account;
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

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, unionUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(unionUser.getId());
        ModuleShow moduleShow = getModuleShow(unionUser, riseMembers);

        //是否买过或曾经买过 商学院/专业版
        Boolean isMember = riseMembers.stream().anyMatch(item -> (item.getMemberTypeId() == RiseMember.CAMP));
        Profile profile = accountService.getProfile(unionUser.getId());

        if (isMember && isPersonalInfoInComplete(profile)) {
            // 未填写信息的已购买专项课的 “新” 会员
            response.sendRedirect(PROFILE_CAMP_SUBMIT);
            return null;
        } else if (whiteListService.isGoCampCountDownPage(unionUser.getId())) {
            // 填完身份信息之后，开始学习日期未到
            response.sendRedirect(CAMP_COUNT_DOWN_URL);
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

    @RequestMapping(value = {"/rise/static/**"}, method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response, UnionUser unionUser,
                                 @RequestParam(value = "_tm", required = false) String channel) throws Exception {
        logger.info("进入 rise/static/**");
        if (unionUser == null) {
            logger.info("unionUser 为空");
            WebUtils.auth(request, response);
            return null;
        }
        logger.info("unionUser 不为空");

        String accessToken = CookieUtils.getCookie(request, OAuthService.MOBILE_STATE_COOKIE_NAME);
        String openid;
        Account account;
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

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, unionUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        // 加载首屏广告信息
        activityMessageService.loginMsg(unionUser.getId());

        List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(unionUser.getId());
        ModuleShow moduleShow = getModuleShow(unionUser, riseMembers);

        return courseView(request, response, channel, moduleShow, RISE_VIEW);
    }

    private ModuleShow getModuleShow(UnionUser unionUser, List<RiseMember> riseMembers) {
        // 菜单白名单 ,之后正式开放时，可以先在zk里关掉test，之后有时间在删掉这段代码，包括前后端,jsp
        ModuleShow moduleShow = new ModuleShow();

        // 是否显示发现tab
        // 谁不显示：有课程计划表则不显示
        Boolean showExplore = whiteListService.isShowExploreTab(unionUser.getId(), riseMembers);
        moduleShow.setShowExplore(showExplore);
        return moduleShow;
    }

    @RequestMapping(value = "/rise/index/msg", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getIndexMsg(UnionUser unionUser) {
        if (unionUser == null) {
            logger.info("游客访问");
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

        //设置渠道漏洞监控参数,浏览器关闭后cookie自动失效
        if (channel != null) {
            CookieUtils.addCookie("_tm", channel, response);
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
