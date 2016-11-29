package com.iquanwai.platon.web.weixin;

import com.iquanwai.platon.biz.po.Callback;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.util.CookieUtils;
import com.iquanwai.platon.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 8/14/14.
 */
@RequestMapping("/wx/oauth")
@Controller
public class OAuthController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping("/auth")
    public void oauthCode(@RequestParam("callbackUrl") String callbackUrl,
                          HttpServletResponse response) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("callbackUrl is " + callbackUrl);
            }
            String requestUrl = oAuthService.redirectUrl(callbackUrl);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("requestUrl is " + requestUrl);
            }
            response.sendRedirect(requestUrl);

        }catch (Exception e){
            LOGGER.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/code")
    public void oauthCode(@RequestParam(required=false) String code,
                            @RequestParam String state,
                            HttpServletResponse response) {
        try {
            if (code == null) {
                //用户不同意授权,跳转报错页面
                return;
            }

            // 返回带accessToken的url
            Callback callback = oAuthService.accessToken(code, state);
            if(callback==null){
                response.sendRedirect("/403.jsp");
            }else {
                LOGGER.info("set _act {} for {} ", callback.getAccessToken(), callback.getOpenid());
                //在cookie中写入access_token
                CookieUtils.addCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME,
                        callback.getAccessToken(), OAuthService.SEVEN_DAYS, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        }catch (Exception e){
            LOGGER.error("code failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/openid/{access_token}")
    public ResponseEntity<Map<String, Object>> openid(@PathVariable("access_token") String accessToken) {
        try {
            String openid = oAuthService.openId(accessToken);
            LOGGER.info("openId {}, accessToken {}", openid, accessToken);
            return WebUtils.result(openid);
        }catch (Exception e){
            LOGGER.error("openid failed", e);
        }
        return WebUtils.error("accessToken is expired");
    }

    @RequestMapping("/refresh/{access_token}")
    public ResponseEntity<Map<String, Object>> refresh(@PathVariable("access_token") String accessToken) {
        try {
            String newAccessToken = oAuthService.refresh(accessToken);
            return WebUtils.result(newAccessToken);
        }catch (Exception e){
            LOGGER.error("refresh failed", e);
        }
        return WebUtils.error("refresh failed");
    }


}
