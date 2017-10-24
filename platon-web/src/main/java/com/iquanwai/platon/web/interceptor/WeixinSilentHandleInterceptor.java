package com.iquanwai.platon.web.interceptor;

import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WeixinSilentHandleInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ConfigUtils.isDebug()) {
            // 前端debug开启时,不校验
            if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
                return true;
            }
            String value = CookieUtils.getCookie(request, LoginUserService.ACCESS_ASK_TOKEN_COOKIE_NAME);
            logger.info("Silent interceptor:{}", value);
            if (StringUtils.isEmpty(value)) {
                WebUtils.auth(request, response);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }
}