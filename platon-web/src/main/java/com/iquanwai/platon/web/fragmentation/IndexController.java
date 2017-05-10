package com.iquanwai.platon.web.fragmentation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/rise/static/**",method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid=null;
        Account account=null;
        if(accessToken!=null){
            openid = oAuthService.openId(accessToken);
            account = accountService.getAccount(openid, false);
        }

        logger.info("account:{}", account);
        if (account != null && account.getSubscribe() != null && account.getSubscribe() == 0) {
            // 未关注
            response.sendRedirect(ConfigUtils.adapterDomainName() + "/static/subscribe");
            return null;
        }
        if(!checkAccessToken(request, openid) || account==null){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        if(ConfigUtils.prePublish()){
            // 是否预发布
            boolean inWhite = whiteListService.isInWhiteList(WhiteList.FRAG_PRACTICE, openid);
            if(!inWhite){
                response.sendRedirect("/403.jsp");
                return null;
            }
        }

        if(ConfigUtils.isDevelopment()){
            //如果不在白名单中,直接403报错
            boolean result = whiteListService.isInWhiteList(WhiteList.TEST, loginUser.getOpenId());
            if(!result){
                response.sendRedirect("/403.jsp");
                return null;
            }
        }


        return courseView(request, account);
    }

    private boolean checkAccessToken(HttpServletRequest request, String openid){
        if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
            return true;
        }

        if(StringUtils.isEmpty(openid)){
            return false;
        }
        
        return true;
    }

    private ModelAndView courseView(HttpServletRequest request, Account account){
        ModelAndView mav = new ModelAndView("course");
        if(request.getParameter("debug")!=null){
            if(ConfigUtils.isFrontDebug()){
                mav.addObject("resource", "http://0.0.0.0:4000/bundle.js");
            }else{
                mav.addObject("resource", ConfigUtils.staticResourceUrl());
            }
        }else{
            mav.addObject("resource", ConfigUtils.staticResourceUrl());
        }

        Map<String, String> userParam = Maps.newHashMap();
        userParam.put("userName", account.getNickname());
        userParam.put("headImage",account.getHeadimgurl());
        mav.addAllObjects(userParam);

        return mav;
    }
}
