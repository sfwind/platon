package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.po.common.Profile;
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
import java.util.Collection;

/**
 * Created by tomas on 3/17/16.
 */
public class LoginUserResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;
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
                return loginUserService.getLoginUser(request.getParameter("debug"));
            }
        }
        String accessToken = loginUserService.getToken(request);

        if (loginUserService.isLogin(request)) {
            LoginUser loginUser = loginUserService.getLoginUser(request).getRight();
            // 之前不是会员的才需要立刻刷新一下,会员过期会在job 里跑
            if (!loginUser.getRiseMember()) {
                Profile profile = accountService.getProfile(loginUser.getOpenId(), false);
                if (profile != null && profile.getRiseMember()) {
                    loginUser.setRiseMember(true);
                }
            }
            return loginUser;
        }


        LoginUserService.Platform platform = loginUserService.checkPlatform(request);


        String openId = loginUserService.openId(platform, accessToken);
        if(StringUtils.isEmpty(openId)){
            logger.error("accessToken {} is not found in db", accessToken);
            return null;
        }

        LoginUser loginUser = loginUserService.getLoginUser(openId);
        if (loginUser == null) return null;

        loginUserService.login(platform, accessToken, loginUser);

        return loginUser;
    }


    public static Collection<LoginUser> getAllUsers(){
        return LoginUserService.getAllUsers();
    }
}
