package com.iquanwai.platon.biz.util.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DaoAspect {
    @Pointcut(value = "execution(* com.iquanwai.platon.biz.dao..*.*(..))")
    public void pointCut() {

    }

    @Around(value = "pointCut()")
    public Object aroundMethod(ProceedingJoinPoint pjp) {
        MethodSignature joinPointObject = (MethodSignature) pjp.getSignature();
        Method method = joinPointObject.getMethod();
        Class clazz= pjp.getTarget().getClass();

        Transaction t = Cat.newTransaction("SQL", clazz.getSimpleName() + "." + method.getName());

        try {
            Object result = pjp.proceed();
            t.setStatus(Transaction.SUCCESS);
            return result;
        } catch (Throwable e) {
            t.setStatus(e);
            Cat.logError(e);
            return null;
        } finally {
            t.complete();
        }
    }
}
