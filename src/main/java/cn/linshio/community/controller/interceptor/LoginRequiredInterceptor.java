package cn.linshio.community.controller.interceptor;

import cn.linshio.community.annotation.LoginRequired;
import cn.linshio.community.util.HostHolder;
import org.apache.ibatis.javassist.bytecode.Mnemonic;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的对象 如果是方法就执行
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            //看看拦截对象的方法上是否有@LoginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //如果该方法上有loginRequired注解并且用户不存在就进行重定向，不予许访问
            if (loginRequired!=null && hostHolder.getUser()==null){
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
