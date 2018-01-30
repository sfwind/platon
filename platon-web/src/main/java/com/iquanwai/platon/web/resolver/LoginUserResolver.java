package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class LoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private UnionUserService unionUserService;

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

        if (callback == null) return null;

        // callback 为空的话，会在 interceptor 那层拦截掉
        Assert.notNull(callback, "callback 不能为空");
        Assert.notNull(callback.getUnionId(), "callback 的 UnionId 不能为空");

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        LoginUser loginUser = adapterLoginUser(unionUser);
        logger.info("获取 adapter loginUser 用户，id：{}", loginUser.getId());
        return loginUser;
    }

    private LoginUser adapterLoginUser(UnionUser unionUser) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(unionUser.getId());
        loginUser.setOpenId(unionUser.getOpenId());
        loginUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        loginUser.setWeixinName(unionUser.getNickName());
        loginUser.setUnionId(unionUser.getUnionId());
        return loginUser;
    }
}
