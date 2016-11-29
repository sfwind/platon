package com.iquanwai.platon.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomas on 3/17/16.
 */
@Component
public class ExceptionResolver implements HandlerExceptionResolver, Ordered {
    Logger logger = LoggerFactory.getLogger(getClass());

    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, Exception e) {

//        List<String> excludedException = Lists.newArrayList(StringUtils.split(ConfigUtils.getUnhandledException(), ","));
//
//        if (excludedException.contains(e.getCachedClass().getSimpleName())) {
//            return null;
//        }

        logger.error(e.getMessage(), e);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", 500);
        map.put("msg", "服务器内部错误");

        return new ModelAndView(new MappingJackson2JsonView(), map);

    }

    //Spring加载Ordered的bean，值越小，优先级越高
    //保证在spring默认的Excepton Resolver (order = 0)之前。
    public int getOrder() {
        return -1;
    }
}
