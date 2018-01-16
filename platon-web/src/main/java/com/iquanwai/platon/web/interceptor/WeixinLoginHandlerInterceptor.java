package com.iquanwai.platon.web.interceptor;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

public class WeixinLoginHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是 debug 模式，不进行拦截校验
        if (ConfigUtils.isDebug()) {
            logger.info("处理 debug 模式，不进行拦截校验");
            return true;
        }

        // 如果前端开启了 debug 模式，不进行拦截校验
        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            logger.info("前端开启 debug 模式，不进行拦截校验");
            return true;
        }

        LoginUser.Platform platform = loginUserService.getPlatformType(request);
        // 如果是小程序请求，直接通过，避免拦截 code 换取请求
        if (LoginUser.Platform.WE_MINI.equals(platform)) {
            return true;
        }

        logger.info("进入接口拦截器");

        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            System.out.println("header = " + headers.nextElement() + "  value = " + request.getHeader(headers.nextElement()));
        }

        LoginUser loginUser = loginUserService.getLoginUserByRequest(request);
        if (loginUser != null) {
            return true;
        } else {
            switch (platform) {
                case PC:
                    writeUnLoginPage(response);
                    return false;
                case WE_MOBILE:
                    WebUtils.auth(request, response);
                    return false;
                case WE_MINI:
                    return true;
                default:
                    writeUnLoginPage(response);
                    return false;
            }
        }
    }

    /**
     * 认定没有登录的请求，输出登录提示
     * @param response 请求返回
     */
    private void writeUnLoginPage(HttpServletResponse response) throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        map.put("code", 401);
        map.put("msg", "没有登录");
        out.write(CommonUtils.mapToJson(map));
    }

}
