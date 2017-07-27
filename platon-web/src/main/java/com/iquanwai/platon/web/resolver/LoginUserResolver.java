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

    public boolean supportsParameter(MethodParameter methodParameter) {
        if (LoginUser.class.isAssignableFrom(methodParameter.getParameterType())) {
            return true;
        }
        return false;
    }

    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        //调试时，返回mock user
        if(ConfigUtils.isDebug()){
            return LoginUser.defaultUser();
        }
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if(request.getParameter("debug")!=null && ConfigUtils.isFrontDebug()){
            //前端调试开启时，如果debug=true,返回mockuser
            if(request.getParameter("debug").equalsIgnoreCase("true")) {
                return LoginUser.defaultUser();
            }else{
                //返回模拟的openid user
                return loginUserService.getLoginUser(request.getParameter("debug"), LoginUserService.Platform.Wechat);
            }
        }
        String accessToken = loginUserService.getToken(request);

        if (loginUserService.isLogin(request)) {
            return loginUserService.getLoginUser(request).getRight();
        }


        LoginUserService.Platform platform = loginUserService.checkPlatform(request);


        String openId = loginUserService.openId(platform, accessToken);
        if(StringUtils.isEmpty(openId)){
            logger.error("accessToken {} is not found in db", accessToken);
            return null;
        }

        LoginUser loginUser = loginUserService.getLoginUser(openId, platform);
        if (loginUser == null) {
            return null;
        }
        logger.info("用户:{}，在resolver重新登录,cookie:{}", openId, accessToken);
        loginUserService.login(platform, accessToken, loginUser);

        return loginUser;
    }

}
