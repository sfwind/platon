package com.iquanwai.platon.web.fragmentation;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionAnswer;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
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
import java.util.Date;
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
    private PlanService planService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private InterlocutionService interlocutionService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //商学院按钮url
    private static final String INDEX_BUSINESS_SCHOOL_URL = "/rise/static/rise";
    //训练营按钮url
    private static final String INDEX_CAMP_URL = "/rise/static/camp";
    //关注页面
    private static final String SUBSCRIBE_URL = "/static/subscribe";
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

    private static final String LOGIN_REDIS_KEY = "login:";
    private static final String WELCOME_MSG_REDIS_KEY = "welcome:msg:";
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
//        return courseView(request, null, false, RISE_VIEW);
    }


    @RequestMapping(value = "/rise/static/guest/note/**", method = RequestMethod.GET)
    public ModelAndView getGuestIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return courseView(request, null, false, NOTE_VIEW);
    }

    @RequestMapping(value = {"/rise/static/guest/**"}, method = RequestMethod.GET)
    public ModelAndView getGuestInterIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("问题／答案页面");
        return courseView(request, null, false, RISE_VIEW);
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

        return courseView(request, loginUser, false, NOTE_VIEW);
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

        // 菜单白名单 ,之后正式开放时，可以先在zk里关掉test，之后有时间在删掉这段代码，包括前后端,jsp
        Boolean showForum = true;
        if (ConfigUtils.isForumTest()) {
            // 论坛处于测试中,在白名单则显示，否则隐藏
            showForum = whiteListService.isInWhiteList(WhiteList.FORUM, loginUser.getId());
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getId());
            if (!result) {
                response.sendRedirect(FORBID_URL);
                return null;
            }
        }

        //点击商学院,非年费用户和小课单买用户跳转售卖页
        if (request.getRequestURI().startsWith(INDEX_BUSINESS_SCHOOL_URL)) {
            //
            List<RiseMember> riseMembers = accountService.loadAllRiseMembersByProfileId(loginUser.getId());
            Boolean isElite = riseMembers.stream().anyMatch(item -> (item.getMemberTypeId() == RiseMember.ELITE || item.getMemberTypeId() == RiseMember.HALF_ELITE));
            Profile profile = accountService.getProfile(loginUser.getId());
            if (isElite && (profile.getAddress() == null || profile.getMobileNo() == null || profile.getIsFull() == 0)) {
                // 如果地址是null，并且是会员，则进入填写信息页面
                response.sendRedirect(PROFILE_SUBMIT);
                return null;
            } else if (whiteListService.isGoToCountDownNotice(loginUser.getId(), riseMembers)) {
                response.sendRedirect(BUSINESS_COUNT_DOWN_URL);
                return null;
            } else if (whiteListService.isGoToScheduleNotice(loginUser.getId(), riseMembers)) {
                response.sendRedirect(SCHEDULE_NOTICE);
                return null;
            } else if (whiteListService.checkRiseMenuWhiteList(loginUser.getId())) {
                // 查看他的会员
                loginMsg(loginUser);
                // 查看点击商学院的时候，是否已经开营
            } else {
                response.sendRedirect(BUSINESS_SCHOOL_SALE_URL);
                return null;
            }
        }

        //点击训练营,非小课训练营用户跳转售卖页
        if (request.getRequestURI().startsWith(INDEX_CAMP_URL)) {
            if (whiteListService.checkCampMenuWhiteList(loginUser.getId())) {
                loginMsg(loginUser);
            } else {
                response.sendRedirect(CAMP_SALE_URL);
                return null;
            }
        }

        return courseView(request, loginUser, showForum, RISE_VIEW);
    }

    @RequestMapping(value = "/rise/index/msg", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getIndexMsg(LoginUser loginUser) {
        if (loginUser == null) {
            logger.info("游客访问");
            return WebUtils.error(202, "游客访问");
        }
        String msg = redisUtil.get(WELCOME_MSG_REDIS_KEY + loginUser.getId());
        ActivityMsg activityMsg = null;
        if (msg != null) {
            logger.info("删除key {}", WELCOME_MSG_REDIS_KEY + loginUser.getId());
            redisUtil.deleteByKey(WELCOME_MSG_REDIS_KEY + loginUser.getId());
            String json = ConfigUtils.getWelcomeMsg();
            Gson gson = new Gson();
            activityMsg = gson.fromJson(json, ActivityMsg.class);
        }

        return WebUtils.result(activityMsg);
    }

    private void loginMsg(LoginUser loginUser) {
        String json = ConfigUtils.getWelcomeMsg();
        if (json != null) {
            Gson gson = new Gson();
            ActivityMsg msg = gson.fromJson(json, ActivityMsg.class);
            Date start = DateUtils.parseStringToDateTime(msg.getStartTime());
            Date end = DateUtils.parseStringToDateTime(msg.getEndTime());
            //获取最后登录时间
            String lastLoginTime = redisUtil.get(LOGIN_REDIS_KEY + loginUser.getId());
            //活动未过期 且已开始
            if (end.after(new Date()) && start.before(new Date())) {
                //很久未登录
                if (lastLoginTime == null) {
                    //保存60秒
                    logger.info("{}很久未登录", loginUser.getId());
                    // ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getId());
                    // //首次登录用户不发活动信息
                    // if (improvementPlan != null) {
                    //     redisUtil.set(WELCOME_MSG_REDIS_KEY + loginUser.getId(), true, 60L);
                    // }
                } else {
                    Date lastLogin = DateUtils.parseStringToDateTime(lastLoginTime);
                    //上次登录时间早于活动开始时间
                    if (lastLogin.before(start)) {
                        //保存60秒
                        logger.info("{}上次登录时间早于活动时间", loginUser.getId());
                        //首次登录用户不发活动信息
                        ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getId());
                        if (improvementPlan != null) {
                            redisUtil.set(WELCOME_MSG_REDIS_KEY + loginUser.getId(), true, 60L);
                        }
                    } else {
                        logger.info("{}上次登录时间晚于活动时间", loginUser.getId());
                    }
                }
            } else {
                logger.info("活动已过期", loginUser.getId());
            }
        }

        //保存30天最后登录时间
        redisUtil.set(LOGIN_REDIS_KEY + loginUser.getId(),
                DateUtils.parseDateTimeToString(new Date()), 60 * 60 * 24 * 30L);
    }

    private boolean checkAccessToken(HttpServletRequest request, String openid) {
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return true;
        }

        return !StringUtils.isEmpty(openid);
    }

    private ModelAndView courseView(HttpServletRequest request, LoginUser account, Boolean showForum, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String resourceUrl;
        switch (viewName) {
            case RISE_VIEW:
                resourceUrl = ConfigUtils.staticResourceUrl();
                break;
            case NOTE_VIEW:
                resourceUrl = ConfigUtils.staticNoteResourceUrl();
                break;
            default:
                resourceUrl = ConfigUtils.staticResourceUrl();
        }
//        String vendorUrl = ConfigUtils.vendorResourceUrl();
        if (request.isSecure()) {
            resourceUrl = resourceUrl.replace("http:", "https:");
        }
        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
//                mav.addObject("vendorResource", "http://0.0.0.0:4000/vendor.js");
            } else {
                mav.addObject("resource", resourceUrl);
//                mav.addObject("vendorResource", vendorUrl);
            }
        } else {
            mav.addObject("resource", resourceUrl);
//            mav.addObject("vendorResource", vendorUrl);
        }

        Map<String, String> userParam = Maps.newHashMap();
        userParam.put("userName", account != null ? account.getWeixinName() : "");
        if (account != null && account.getHeadimgUrl() != null) {
            userParam.put("headImage", account.getHeadimgUrl().replace("http:", "https:"));
        }
        mav.addAllObjects(userParam);
        mav.addObject("showForum", showForum);

        return mav;
    }
}
