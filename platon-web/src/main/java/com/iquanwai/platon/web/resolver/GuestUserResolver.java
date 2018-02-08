package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Account;
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

public class GuestUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UnionUserService unionUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return GuestUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        if (ConfigUtils.isDebug()) {
            return GuestUser.defaultUser();
        }

        logger.info("--------------------");
        logger.info("进入 guestUser resolver");
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Callback callback = unionUserService.getCallbackByRequest(request);
        if (callback == null) {
            return null;
        }

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        GuestUser guestUser = null;
        if (unionUser != null) {
            logger.info("加载 UnionUserId: {}, UnionId: {}", unionUser.getId(), unionUser.getUnionId());
            guestUser = adapterUnionUser(unionUser);
        }
        return guestUser;
    }

    private GuestUser adapterUnionUser(UnionUser unionUser) {
        Account account = accountService.getAccountByUnionId(unionUser.getUnionId());
        GuestUser guestUser = new GuestUser();
        guestUser.setId(unionUser.getId());
        guestUser.setUnionId(unionUser.getUnionId());
        guestUser.setOpenId(unionUser.getOpenId());
        guestUser.setWeixinName(unionUser.getNickName());
        guestUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        guestUser.setRealName(account.getRealName());
        guestUser.setSubscribe(account.getSubscribe() == 1);
        return guestUser;
    }


}
