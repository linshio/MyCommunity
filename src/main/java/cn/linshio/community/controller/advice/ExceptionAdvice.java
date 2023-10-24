package cn.linshio.community.controller.advice;

import cn.linshio.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//异常处理类
@ControllerAdvice(annotations = Controller.class)//只扫描带有controller的类
@Slf4j
public class ExceptionAdvice {

    //当controller 发生Exception异常时会调用这个类 记录异常日志
    @ExceptionHandler(value = Exception.class)
    public void handleException(Exception e , HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器发生异常"+e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            log.error(stackTraceElement.toString());
        }
        //这里进行判断请求是普通的请求还是异步的请求 普通请求直接返回页面
        String xRequestedWith = request.getHeader("x-requested-with");
        //如果为异步请求 就向浏览器响应json报错数据并跳转到错误的页面
        if ("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.println(CommunityUtil.getJSONString(500,"服务器异常"));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}
