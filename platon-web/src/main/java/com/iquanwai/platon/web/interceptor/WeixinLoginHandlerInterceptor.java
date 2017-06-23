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
            String value = loginUserService.getToken(request);
            if (StringUtils.isEmpty(value)) {
                switch (platform) {
                    case Wechat:
                        logger.error("拦截器，微信端重定向");
                        WebUtils.auth(request, response);
                        return false;
                    case PC:{
                        boolean cookieInvalid = false;
                        if (StringUtils.isEmpty(value)) {
                            cookieInvalid = true;
                        } else {
                            // cookie 不为空
                            if (!loginUserService.isLogin(request)) {
                                // 有cookie，但是没有登录
                                Pair<Integer, Callback> pair = loginUserService.refreshLogin(platform,value);
                                if (pair.getLeft() < 1) {
                                    cookieInvalid = true;
                                }
                                // 否则通过
                            }
                        }
                        if(cookieInvalid){
                            logger.error("拦截器，pc端重定向");
                            Map<String, Object> map = Maps.newHashMap();
                            PrintWriter out = response.getWriter();
                            map.put("code", 401);
                            map.put("msg", "没有登录");
                            out.write(CommonUtils.mapToJson(map));
                            return false;
                        }
                    }
                }

            }
        }
        return true;

    }

}
