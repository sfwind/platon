package com.iquanwai.platon.web.aspect;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.util.CookieUtils;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by justin on 16/12/19.
 */
//@ControllerAdvice
public class ExceptionAspect {
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private OperationLogService operationLogService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, Object>> jsonErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        String act = CookieUtils.getCookie(req, OAuthService.MOBILE_STATE_COOKIE_NAME);
        String openid = "";
        if (act != null) {
            openid = oAuthService.openId(act);
        } else {
            String qt = CookieUtils.getCookie(req, OAuthService.PC_STATE_COOKIE_NAME);
            if (qt != null) {
                openid = oAuthService.pcOpenId(qt);
            }
        }
        Cookie[] cookies = req.getCookies();
        String cookie = "";
        if (cookies != null && cookies.length > 0) {
            for (Cookie item : cookies) {
                cookie += item.getName() + ":" + item.getValue() + "; ";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("exception:");
        sb.append(e.getClass().getSimpleName());
        sb.append("url:");
        sb.append(req.getRequestURI());
        sb.append(";queryString:");
        sb.append(req.getQueryString());
        sb.append(";userAgent:");
        sb.append(req.getHeader("user-agent"));
        sb.append(";ip:");
        sb.append(req.getHeader("X-Forwarded-For"));
        sb.append(";cookie:");
        sb.append(cookie);
        String memo;
        if (sb.length() > 1024) {
            memo = sb.substring(0, 1024);
        } else {
            memo = sb.toString();
        }

        OperationLog operationLog = OperationLog.create().openid(openid).module("后端报错")
                .function("后端报错").action("bug").memo(memo);
        operationLogService.log(operationLog);
        logger.error("openId:" + openid + ",uri:" + req.getRequestURI() +
                ",queryString:" + req.getQueryString() +
                ",userAgent:" + req.getHeader("user-agent") +
                ",ip:" + req.getHeader("X-Forwarded-For") +
                ",cookie:" + cookie, e);
        return WebUtils.error("服务器伐开心,我们正在想办法");
    }
}
