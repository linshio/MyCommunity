package cn.linshio.community.controller.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
@Slf4j
public class ServiceLogAspect {

    //声明切入点
    @Pointcut("execution(* cn.linshio.community.service.*.*(..))")
    public void pointCut(){}

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        //记录日志 用户「ip」 在「time」时间访问了「service方法」
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //获取当前请求的ip
            String ip = request.getRemoteHost();
            //获取当前的时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String functionName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
            log.info(String.format("用户「%s」 在「%s」时间访问了「%s」",ip,currentTime,functionName));
        }
    }
}
