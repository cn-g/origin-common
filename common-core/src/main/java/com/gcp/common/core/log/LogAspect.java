package com.gcp.common.core.log;

import com.alibaba.fastjson2.JSON;
import com.gcp.common.core.util.ToolsUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 打印接口调用日志
 * @author Admin
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(* com.gcp.*.*.controller.*.*.*(..))")
    public void controllerMethod(){}

    @Before("controllerMethod()")
    public void logRequestInfo(JoinPoint joinPoint){
        RequestAttributes requestAttribute = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttribute).getRequest();
        // 打印请求内容
        log.info("-----------------------------接口调用发起--------------------------");
        log.info("接收到请求，请求方式={},请求地址={},请求IP={},请求方法名称={},请求参数={}",request.getMethod(),request.getRequestURL().toString(),
                ToolsUtil.getServerIp(),joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 进入方法请求执行后
     * @param o
     * @throws Exception
     */
    @AfterReturning(returning = "o", pointcut = "controllerMethod()")
    public void logResultInfo(Object o){
        log.info("后台接口调用成功:返回结果=" + JSON.toJSONString(o));
        log.info("------------------------------接口调用结束---------------------------");
    }

    /**
     * 该切面发生异常信息时进行拦截
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "controllerMethod()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        System.out.println("后台接口调用失败，连接点方法为：" + methodName + ",参数为：" + args + ",异常为：" + e);
        log.info("----------------------------接口调用结束---------------------------");
    }

}
