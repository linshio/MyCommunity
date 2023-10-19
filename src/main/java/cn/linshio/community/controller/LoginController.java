package cn.linshio.community.controller;

import cn.linshio.community.entity.User;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户注册模块
 */
@Controller
public class LoginController implements CommunityConstant {

    @Resource
    private UserService userService;

    //注册页面
    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    //登录页面
    @GetMapping("/login")
    public String loginPage(){
        return "/site/login";
    }

    //提交注册请求
    @PostMapping("/register")
    public String registerUser(Model model, User user){
        //用户注册成功后我们先让跳转到operate-result页面进行临时处理，这样不会显得比较突兀
        //在临时页面处理完毕后跳转到首页
        Map<Object, Object> map = userService.register(user);
        //如果map为空，也就是没有发生问题下
        if (map==null || map.isEmpty()){
            model.addAttribute("msg","您的账户已经注册成功，请尽快前往您的邮箱进行激活");
            model.addAttribute("target","/index");
        }else {
            //如果发生问题就发回注册页面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
        return "/site/operate-result";
    }

    //处理激活状态
    // http://localhost:8080/community/activation/101/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") Integer userId,
                             @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","您的账户激活成功，已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if (result==ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，您的账户已经激活");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，激活码错误");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
}

