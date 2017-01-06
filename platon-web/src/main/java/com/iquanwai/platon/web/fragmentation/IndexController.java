package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.domain.whitelist.WhiteListService;
import com.iquanwai.platon.biz.po.Account;
import com.iquanwai.platon.biz.po.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @RequestMapping(value = "/fragment/**",method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String accessToken = CookieUtils.getCookie(request, OAuthService.ACCESS_TOKEN_COOKIE_NAME);
        String openid = oAuthService.openId(accessToken);
        if(!checkAccessToken(request, openid)){
            CookieUtils.removeCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME, response);
            WebUtils.auth(request, response);
            return null;
        }
        // TODO: remove later
        boolean inWhite = whiteListService.isInWhiteList(WhiteList.FRAG_PRACTICE, openid);
        if(!inWhite){
            response.sendRedirect("/403.jsp");
            return null;
        }

        return courseView(request);
    }

    private boolean checkAccessToken(HttpServletRequest request, String openid){
        if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
            return true;
        }

        if(StringUtils.isEmpty(openid)){
            return false;
        }

        Account account = accountService.getAccount(openid, false);
        
        return account!=null;
    }

    private ModelAndView courseView(HttpServletRequest request){
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

        return mav;
    }
}
