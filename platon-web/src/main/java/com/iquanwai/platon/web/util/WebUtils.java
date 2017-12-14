package com.iquanwai.platon.web.util;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin on 7/23/15.
 */
public class WebUtils {
    public static ResponseEntity<Map<String, Object>> success() {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", "ok");

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> result(Object result) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", result);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 221);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg, HttpStatus status) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, status);
    }

    public static ResponseEntity<Map<String, Object>> forbid(Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 403);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    /**
     * 默认授权方式，静默授权
     */
    public static void auth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String domainName = request.getHeader("Host");
        String url;
        if(domainName != null){
            url = "http://" + domainName + request.getRequestURI();
        }else{
            url = ConfigUtils.adapterDomainName() + request.getRequestURI();
        }

        if (!StringUtils.isEmpty(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");

        response.sendRedirect(ConfigUtils.adapterDomainName() + "/wx/oauth/auth?callbackUrl=" + url);
    }

    /**
     * 提示性授权
     */
    public static void askAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String domainName = request.getHeader("Host");
        String url;
        if(domainName != null){
            url = "http://" + domainName + request.getRequestURI();
        }else{
            url = ConfigUtils.adapterDomainName() + request.getRequestURI();
        }

        if (!StringUtils.isEmpty(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");

        response.sendRedirect(ConfigUtils.adapterDomainName() + "/wx/oauth/auth/ask?callbackUrl=" + url);
    }
}
