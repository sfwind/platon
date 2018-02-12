package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
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
    private UnionUserServiceImpl unionUserService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return LoginUser.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        if (ConfigUtils.isDebug()) {
            return LoginUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback == null) {
            // 特殊处理，被 interceptor 排除，但是却还想获取 user 的接口
            return null;
        }

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        LoginUser loginUser = null;
        if (unionUser != null) {
            loginUser = adapterLoginUser(unionUser);
            UnionUser.Platform platform = unionUserService.getPlatformType(request);
            loginUser.setDevice(platform == UnionUser.Platform.MOBILE ? Constants.Device.MOBILE : Constants.Device.PC);
        } else {
            logger.error("unionUser 为空");
        }
        return loginUser;
    }

    private LoginUser adapterLoginUser(UnionUser unionUser) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(unionUser.getId());
        loginUser.setOpenId(unionUser.getOpenId());
        loginUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        loginUser.setWeixinName(unionUser.getNickName());
        loginUser.setUnionId(unionUser.getUnionId());
        loginUser.setRole();
        return loginUser;
    }
}
