package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by tomas on 3/17/16.
 */
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
        //调试时，返回mock user
        if (ConfigUtils.isDebug()) {
            return LoginUser.defaultUser();
        }
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (LoginUserService.Platform.WE_MINI.equals(loginUserService.checkPlatform(request))) {
            // 如果是小程序发送请求
            String state = request.getHeader("_sk");
            
        }

        if (request.getParameter("debug") != null && ConfigUtils.isFrontDebug()) {
            //前端调试开启时，如果debug=true，返回mockuser
            if ("true".equalsIgnoreCase(request.getParameter("debug"))) {
                return LoginUser.defaultUser();
            } else {
                // 返回模拟的openid user
                return loginUserService.getLoginUser(request.getParameter("debug"), LoginUserService.Platform.WE_MOBILE);
            }
        }
        String accessToken = loginUserService.getToken(request);

        if (loginUserService.isLogin(request)) {
            // 如果用户已经是登陆状态会直接返回
            return loginUserService.getLoginUser(request).getRight();
        } else {
            // 非登陆状态
            LoginUserService.Platform platform = loginUserService.checkPlatform(request);
            String openId = loginUserService.openId(platform, accessToken);
            if (StringUtils.isEmpty(openId)) {
                logger.error("accessToken {} is not found in db", accessToken);
                return null;
            }

            LoginUser loginUser = loginUserService.getLoginUser(openId, platform);
            if (loginUser == null) {
                return null;
            }
            // logger.info("用户:{}，在resolver重新登录,cookie:{}", openId, accessToken);
            if (loginUser.getId() != null) {
                loginUserService.login(platform, accessToken, loginUser);
            }

            return loginUser;
        }
    }
}
