package cn.linshio.community.controller.interceptor;

import cn.linshio.community.entity.LoginTicket;
import cn.linshio.community.entity.User;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CookieUtils;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

//拦截器实现用户信息的展示
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    //在controller方法调用之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtils.getCookieValue(request, "ticket");
        //查询凭证
        LoginTicket loginTicket = userService.selectLoginTicket(ticket);
        //检查凭证是否有效  凭证为空 状态存活 并且凭证还在有效期内
        if (loginTicket!=null && loginTicket.getStatus()==0 &&loginTicket.getExpired().after(new Date())){
            //根据凭证查询用户
            User user = userService.selectUserById(loginTicket.getUserId());
            //在本次请求中持有该用户
            hostHolder.setUser(user);
        }
        //放行
        return true;
    }

    //在controller调用之后 但是在模版引擎调用之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null && modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    //在模版引擎调用之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clean();
    }
}
