package com.iquanwai.platon.web.interceptor;

import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserServiceImpl;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by 三十文
 */
public class HandlerInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UnionUserServiceImpl unionUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UnionUser.Platform platform = WebUtils.getPlatformType(request);
        if (platform == null || unionUserService.isDocumentRequest(request)) {
            return true;
        } else {
            Callback callback = unionUserService.getCallbackByRequest(request);
            if (callback != null && callback.getUnionId() != null) {
                return true;
            } else {
                if (ConfigUtils.isDebug()) {
                    return true;
                } else {
                    return handleUnLogin(response);
                }
            }
        }
    }

    /**
     * 对于 ajax 请求，不存在 callback 请求的处理
     *
     * @param response 响应
     * @return 是否通过拦截器
     */
    private boolean handleUnLogin(HttpServletResponse response) throws Exception {
        writeUnLoginStatus(response);
        return false;
    }

    /**
     * ajax 请求 登录校验不通过，返回未登录 700 状态码
     */
    private void writeUnLoginStatus(HttpServletResponse response) throws IOException {
        Writer writer = null;
        try {
            response.setStatus(700);
            writer = response.getWriter();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
