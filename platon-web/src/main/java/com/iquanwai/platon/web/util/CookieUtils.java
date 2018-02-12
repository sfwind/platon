package com.iquanwai.platon.web.util;

import com.iquanwai.platon.biz.util.ConfigUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by justin on 16/8/26.
 */
public class CookieUtils {
    /**
     * 添加cookie
     * @param name cookie的key
     * @param value cookie的value
     * @param domain domain
     * @param maxAge 最长存活时间 单位为秒
     */
    public static void addCookie(String name, String value, String domain,
                                 int maxAge, String path, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        // cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 往根下面存一个cookie
     * * @param name cookie的key
     * @param value cookie的value
     * @param domain domain
     * @param maxAge 最长存活时间 单位为秒
     */
    public static void addCookie(String name, String value, String domain, int maxAge, HttpServletResponse response) {
        addCookie(name, value, domain, maxAge, "/", response);
    }

    /**
     * 往根下面存一个cookie
     * @param name cookie的key
     * @param value cookie的value
     * @param maxAge 最长存活时间 单位为秒
     */
    public static void addCookie(String name, String value, int maxAge, HttpServletResponse response) {
        addCookie(name, value, ConfigUtils.realDomainName(), maxAge, "/", response);
    }

    /**
     * 从cookie值返回cookie值，如果没有返回 null
     * @return cookie的值
     */
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(name)) {
                return cookies[i].getValue();
            }
        }
        return null;
    }

    public static void removeCookie(String name, String domain, HttpServletResponse response) {
        addCookie(name, null, domain, 0, response);
    }

    public static void removeCookie(String name, HttpServletResponse response) {
        removeCookie(name, null, response);
    }
}
