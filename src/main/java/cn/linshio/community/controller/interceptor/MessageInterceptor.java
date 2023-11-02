package cn.linshio.community.controller.interceptor;

import cn.linshio.community.entity.User;
import cn.linshio.community.service.MessageService;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//使用拦截器实现总消息未读数量
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null && modelAndView!=null){
            int lettersUnread = messageService.findLettersUnread(user.getId(), null);
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount",lettersUnread+unreadNoticeCount);
        }
    }
}
