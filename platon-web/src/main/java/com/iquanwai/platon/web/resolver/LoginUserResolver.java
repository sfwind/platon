package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Callback;
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
    private UnionUserServiceImpl unionUserService;

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

        logger.info("--------------------");
        logger.info("进入 loginuse resolver");
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback == null) {
            // 特殊处理，被 interceptor 排除，但是却还想获取 user 的接口
            logger.info("callback 为空");
            return null;
        }

        logger.info("callback 不为空");
        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        LoginUser loginUser = null;
        if (unionUser != null) {
            logger.info("unionUser id: {}", unionUser.getId());
            logger.info("加载 UnionUserId: {}, UnionId: {}", unionUser.getId(), unionUser.getUnionId());
            loginUser = adapterLoginUser(unionUser);
        } else {
            logger.info("unionUser 为空");
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
        return loginUser;
    }
}
