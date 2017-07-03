package com.iquanwai.platon.web.fragmentation;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.WhiteList;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Created by justin on 16/9/9.
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX_URL = "/rise/static/plan/main";

    private static final String LOGIN_REDIS_KEY = "LOGIN_";
    private static final String WELCOME_MSG_REDIS_KEY = "WELCOME_MSG_";

    @RequestMapping(value = "/rise/static/**", method = RequestMethod.GET)
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
                response.sendRedirect(ConfigUtils.adapterDomainName() + "/static/subscribe");
                return null;
            }
        }

        if (!checkAccessToken(request, openid) || account == null) {
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }

        if (ConfigUtils.prePublish()) {
            // 是否预发布
            boolean inWhite = whiteListService.isInWhiteList(WhiteList.FRAG_PRACTICE, loginUser.getId());
            if (!inWhite) {
                response.sendRedirect("/403.jsp");
                return null;
            }
        }

        if (ConfigUtils.isDevelopment()) {
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getId());
            if (!result) {
                response.sendRedirect("/403.jsp");
                return null;
            }
        }

        if (request.getRequestURI().startsWith(INDEX_URL)) {
            loginMsg(loginUser);
        }

        return courseView(request, account);
    }

    @RequestMapping(value = "/rise/index/msg", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getIndexMsg(HttpServletRequest request, HttpServletResponse response,
                                                           LoginUser loginUser){
        String msg = redisUtil.get(WELCOME_MSG_REDIS_KEY + loginUser.getId());
        ActivityMsg activityMsg = null;
        if(msg != null){
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
            //活动未过期
            if(end.after(new Date())){
                //很久未登录
                if (lastLoginTime == null) {
                    //保存60秒
                    logger.info("{}很久未登录", loginUser.getId());
                    ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getId());
                    //首次登录用户不发活动信息
                    if(improvementPlan!=null){
                        redisUtil.set(WELCOME_MSG_REDIS_KEY + loginUser.getId(), true, 60L);
                    }
                }else{
                    Date lastLogin = DateUtils.parseStringToDateTime(lastLoginTime);
                    //上次登录时间早于活动开始时间
                    if(lastLogin.before(start)){
                        //保存60秒
                        logger.info("{}上次登录时间早于活动时间", loginUser.getId());
                        //首次登录用户不发活动信息
                        ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getId());
                        if(improvementPlan!=null){
                            redisUtil.set(WELCOME_MSG_REDIS_KEY + loginUser.getId(), true, 60L);
                        }
                    }else{
                        logger.info("{}上次登录时间晚于活动时间", loginUser.getId());
                    }
                }
            }else{
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

    private ModelAndView courseView(HttpServletRequest request, Account account) {
        ModelAndView mav = new ModelAndView("course");
        String resourceUrl = ConfigUtils.staticResourceUrl();
        if (request.isSecure()) {
            resourceUrl = resourceUrl.replace("http:", "https:");
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
        userParam.put("userName", account.getNickname());
        if (account.getHeadimgurl() != null) {
            userParam.put("headImage", account.getHeadimgurl().replace("http:", "https:"));
        }
        mav.addAllObjects(userParam);

        return mav;
    }
}
