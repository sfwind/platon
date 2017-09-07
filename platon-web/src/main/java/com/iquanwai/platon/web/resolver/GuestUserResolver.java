package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.util.CookieUtils;
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
 * Created by nethunder on 2017/9/7.
 */
public class GuestUserResolver implements HandlerMethodArgumentResolver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private AccountService accountService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return GuestUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer
            modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        //调试时，返回mock user
        if (ConfigUtils.isDebug()) {
            return LoginUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String value = CookieUtils.getCookie(request, LoginUserService.ACCESS_ASK_TOKEN_COOKIE_NAME);

        Callback callback = callbackDao.queryByAccessToken(value);
        String openid = callback.getOpenid();
        Account account = accountService.getAccount(openid, false);
        if (account == null) {
            return null;
        } else {
            GuestUser guestUser = new GuestUser();
            guestUser.setOpenId(account.getOpenid());
            guestUser.setHeadimgUrl(account.getHeadimgurl());
            guestUser.setWeixinName(account.getNickname());
            guestUser.setRealName(account.getRealName());
            return guestUser;
        }
    }
}
