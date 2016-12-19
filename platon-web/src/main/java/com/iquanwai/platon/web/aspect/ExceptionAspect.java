package com.iquanwai.platon.web.aspect;

import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.util.CookieUtils;
import com.iquanwai.platon.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by justin on 16/12/19.
 */
@ControllerAdvice
public class ExceptionAspect {
    @Autowired
    private OAuthService oAuthService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, Object>> jsonErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        String openid = oAuthService.openId(CookieUtils.getCookie(req, OAuthService.ACCESS_TOKEN_COOKIE_NAME));
        logger.error(openid+" 操作失败", e);
        return WebUtils.error("服务器伐开心,我们正在想办法");
    }
}
