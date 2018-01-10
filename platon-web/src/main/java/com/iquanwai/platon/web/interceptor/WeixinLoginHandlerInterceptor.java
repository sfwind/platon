package com.iquanwai.platon.web.interceptor;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

public class WeixinLoginHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 前端debug开启时,不校验
        if (!ConfigUtils.isDebug()) {
            if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
                return true;
            }
            LoginUserService.Platform platform = loginUserService.checkPlatform(request);
            if (platform == LoginUserService.Platform.WE_MINI) {
                // TODO 如果是小程序登录，不进行校验
                return true;
            }
            String value = loginUserService.getToken(request);
            if (StringUtils.isEmpty(value)) {
                switch (platform) {
                    case WE_MOBILE:
                        WebUtils.auth(request, response);
                        return false;
                    case PC: {
                        boolean cookieInvalid = false;
                        if (StringUtils.isEmpty(value)) {
                            cookieInvalid = true;
                        } else {
                            // cookie 不为空
                            if (!loginUserService.isLogin(request)) {
                                // 有cookie，但是没有登录
                                Pair<Integer, Callback> pair = loginUserService.refreshLogin(platform, value);
                                if (pair.getLeft() < 1) {
                                    cookieInvalid = true;
                                }
                                // 否则通过
                            }
                        }
                        if (cookieInvalid) {
                            Map<String, Object> map = Maps.newHashMap();
                            PrintWriter out = response.getWriter();
                            map.put("code", 401);
                            map.put("msg", "没有登录");
                            out.write(CommonUtils.mapToJson(map));
                            return false;
                        }
                    }
                    default:
                }
            }
        }
        return true;
    }

}
