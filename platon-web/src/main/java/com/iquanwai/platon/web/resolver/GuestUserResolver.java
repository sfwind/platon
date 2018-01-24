package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
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
            return GuestUser.defaultUser();
        }

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String state = CookieUtils.getCookie(request, LoginUserService.WE_CHAT_STATE_COOKIE_NAME);
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            return null;
        }

        String openid = callback.getOpenid();
        Account account;
        try {
            account = accountService.getAccount(openid, false);
        } catch (NotFollowingException e) {
            // 用Ack去请求微信接口
            account = accountService.getGuestFromWeixin(openid, callback.getAccessToken());
        }
        if (account == null) {
            return null;
        } else {
            GuestUser guestUser = new GuestUser();
            guestUser.setOpenId(account.getOpenid());
            guestUser.setHeadimgUrl(account.getHeadimgurl());
            guestUser.setWeixinName(account.getNickname());
            guestUser.setRealName(account.getRealName());
            guestUser.setSubscribe(account.getSubscribe());
            return guestUser;
        }
    }
}
