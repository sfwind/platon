package com.iquanwai.platon.web.aspect;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
  
/** 
 *  
* @ClassName: LogAspect  
* @Description: 日志记录AOP实现  
*
 */  
@Aspect
@Component
public class LogAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginUserService loginUserService;
  
    /** 
     *  
     * @Title：doAround 
     * @Description: 环绕触发  
     * @param pjp
     * @return 
     * @throws Throwable 
     */
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;  
        HttpServletRequest request = sra.getRequest();
        // 获取输入参数  
        Map<?,?> inputParamMap = request.getParameterMap();
        // 获取请求地址  
        String requestPath = request.getRequestURI();
        String userName = null;

        // 执行完方法的返回值：调用proceed()方法，就会触发切入点方法执行  
        Map<String, Object> outputParamMap = new HashMap<String, Object>();
        long startTimeMillis = System.currentTimeMillis();
        Object result = pjp.proceed();// result的值就是被拦截方法的返回值
        long endTimeMillis = System.currentTimeMillis();
        outputParamMap.put("result", result);

        //超长请求也需要打印日志
        if(ConfigUtils.logDetail()||endTimeMillis-startTimeMillis>=1000) {
            Gson gson = new Gson();
            String optTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTimeMillis);
            LoginUser loginUser = loginUserService.getLoginUser(request).getRight();
            if (loginUser != null) {
                userName = loginUser.getWeixinName();
            }
            logger.info("\n user：" + userName
                    + "  url：" + requestPath + "; op_time：" + optTime + " pro_time：" + (endTimeMillis - startTimeMillis) + "ms ;"
                    + " param：" + gson.toJson(inputParamMap) + ";" + "\n result：" + gson.toJson(outputParamMap));
        }
        return result;  
    }  

}  