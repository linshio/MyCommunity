package cn.linshio.community.util;


import cn.linshio.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，代替session对象 线程安全
 */
@Component
public class HostHolder {
    private ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void setUser(User user){
        threadLocal.set(user);
    }

    public User getUser(){
        return threadLocal.get();
    }

    public void clean(){
        threadLocal.remove();
    }
}
