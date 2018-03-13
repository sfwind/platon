package com.iquanwai.platon.web.util;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by justin on 7/23/15.
 */
public class WebUtils {
    public static ResponseEntity<Map<String, Object>> success() {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", "ok");

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> result(Object result) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", result);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 221);
        json.put("msg", msg);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg, HttpStatus status) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<>(json, status);
    }

    public static ResponseEntity<Map<String, Object>> forbid(Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 403);
        json.put("msg", msg);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * 默认授权方式，静默授权
     */
    public static void auth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String domainName = request.getHeader("Host-Test");
        String url;
        if (domainName != null) {
            url = "http://" + domainName + request.getRequestURI();
        } else {
            url = ConfigUtils.adapterDomainName() + request.getRequestURI();
        }
        if (!StringUtils.isEmpty(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");
        response.sendRedirect(ConfigUtils.adapterDomainName() + "/wx/oauth/auth?callbackUrl=" + url);
    }

    /**
     * 获取推广活动时加的漏斗渠道
     */
    public static String getChannel(HttpServletRequest request) {
        String channel = CookieUtils.getCookie(request, "_tm");

        if (channel == null) {
            channel = "";
        }

        return channel;
    }

    public static UnionUser.Platform getPlatformType(HttpServletRequest request) {
        String platformHeader = request.getHeader(UnionUserService.PLATFORM_HEADER_NAME);
        if (platformHeader == null) {
            // 资源请求，没有 platform header，查看 cookie 值
            String pcState = CookieUtils.getCookie(request, UnionUserService.PC_STATE_COOKIE_NAME);
            if (pcState != null) {
                platformHeader = UnionUser.PlatformHeaderValue.PC_HEADER;
            }

            String mobileState = CookieUtils.getCookie(request, UnionUserService.MOBILE_STATE_COOKIE_NAME);
            if (mobileState != null) {
                platformHeader = UnionUser.PlatformHeaderValue.MOBILE_HEADER;
            }
        }

        if (platformHeader != null) {
            switch (platformHeader) {
                case UnionUser.PlatformHeaderValue.PC_HEADER:
                    return UnionUser.Platform.PC;
                case UnionUser.PlatformHeaderValue.MOBILE_HEADER:
                    return UnionUser.Platform.MOBILE;
                case UnionUser.PlatformHeaderValue.MINI_HEADER:
                    return UnionUser.Platform.MINI;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }
}
