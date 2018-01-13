package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class LoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return LoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // 如果是 debug 状态，直接返回默认用户信息
        if (ConfigUtils.isDebug()) {
            return LoginUser.defaultUser();
        }

        // 获取本次请求信息
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            return LoginUser.defaultUser();
            // TODO 临时关闭前端 debug 模式的 OpenId 获取用户身份
        }

        return loginUserService.getLoginUserByRequest(request);
    }
}
