package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
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
import java.util.Map;

public class GuestUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RestfulHelper restfulHelper;

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

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        Callback callback = unionUserService.getCallbackByRequest(request);
        // callback 为空的话，会在 interceptor 那层拦截掉
        Assert.notNull(callback, "callback 不能为空");
        Assert.notNull(callback.getUnionId(), "callback 的 UnionId 不能为空");

        UnionUser unionUser = unionUserService.getUnionUserByCallback(callback);
        if (unionUser == null) {
            String requestUrl = ConfigUtils.domainName() + "/wx/oauth/init/user?state=" + callback.getState();
            String body = restfulHelper.get(requestUrl);
            Map<String, Object> result = CommonUtils.jsonToMap(body);
            String code = result.get("code").toString();
            if ("200".equals(code)) {
                unionUser = unionUserService.getUnionUserByCallback(callback);
            }
        }

        GuestUser guestUser = null;
        if (unionUser != null) {
            logger.info("加载 UnionUserId: {}, UnionId: {}", unionUser.getId(), unionUser.getUnionId());
            guestUser = adapterUnionUser(unionUser);
        }
        return guestUser;
    }

    private GuestUser adapterUnionUser(UnionUser unionUser) {
        Account account = accountService.getAccountByUnionId(unionUser.getUnionId());
        Assert.notNull(account);
        GuestUser guestUser = new GuestUser();
        guestUser.setId(unionUser.getId());
        guestUser.setOpenId(unionUser.getOpenId());
        guestUser.setWeixinName(unionUser.getNickName());
        guestUser.setHeadimgUrl(unionUser.getHeadImgUrl());
        guestUser.setRealName(account.getRealName());
        guestUser.setSubscribe(account.getSubscribe());
        return guestUser;
    }


}
