package com.iquanwai.platon.web.interceptor;

import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by justin on 16/8/26.
 */
public class WeixinLoginHandlerInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginUserService loginUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!ConfigUtils.isDebug()) {
            // 前端debug开启时,不校验
            if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
                return true;
            }
            LoginUserService.Platform platform = loginUserService.checkPlatform(request);
            logger.info("拦截器，平台:{}", platform);
            String value = loginUserService.getToken(request);
            if (StringUtils.isEmpty(value)) {
                switch (platform) {
                    case Wechat:
                        WebUtils.auth(request, response);
                        return false;
                    case PC:
                        logger.error("不该有pc");
                        return false;
                }

            }
        }
        return true;

    }

}
