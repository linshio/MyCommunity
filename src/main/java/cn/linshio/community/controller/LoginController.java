package cn.linshio.community.controller;

import cn.linshio.community.entity.User;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户注册模块
 */
@Controller
@Slf4j
public class LoginController implements CommunityConstant {

    @Resource
    private UserService userService;

    @Autowired
    private Producer kaptcha;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
    public String activation(Model model, @PathVariable("userId") int userId,
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

    //kaptcha 验证码验证
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
        //颁发临时的Cookie
        String kaptchaOwner = CommunityUtil.getRandomUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(600);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入到redis中
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        //设置60S过期
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            log.error("响应状态码失败"+e.getMessage());
        }
    }

    /**
     *
     * @param username 用户输入的用户名
     * @param password 用户输入的密码
     * @param code  用户输入的验证码
     * @param rememberMe 前台的勾选（是否记住）
     * @param model 视图
     * @param kaptchaOwner cookie中的临时凭据
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password,String code, boolean rememberMe,
                        Model model,HttpServletResponse response,@CookieValue("kaptchaOwner")String kaptchaOwner){
        String kaptcha = null;
        //如果不为空就从redis中取出数据
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        //判空验证码 /site/login
        if (code == null || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg","验证码不存在或者验证码错误");
            return "/site/login";
        }
        //检查账号与密码
        int expireTime = rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expireTime);
        if (map.containsKey("ticket")){
            //此处要将cookie携带到客户端
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expireTime);
            response.addCookie(cookie);
            return "redirect:index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 用户登出操作
     * @param ticket 取出浏览器中的cookie
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    //todo：开发忘记密码的功能：
    //
    //- 点击登录页面上的“忘记密码”链接，打开忘记密码页面。
    //
    //- 在表单中输入注册的邮箱，点击获取验证码按钮，服务器为该邮箱发送一份验证码。
    //
    //- 在表单中填写收到的验证码及新密码，点击重置密码，服务器对密码进行修改。

}

