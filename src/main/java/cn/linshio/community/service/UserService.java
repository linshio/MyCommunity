package cn.linshio.community.service;

import cn.linshio.community.dao.LoginTicketMapper;
import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.LoginTicket;
import cn.linshio.community.entity.User;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.MailClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class UserService implements CommunityConstant {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private MailClient mailClient;

    @Resource
    private LoginTicketMapper loginTicketMapper;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    //查询用户
    public User selectUserById(Integer id){
        return userMapper.selectUserById(id);
    }

    //查询凭证
    public LoginTicket selectLoginTicket(String ticket){
        return loginTicketMapper.selectLoginTicketByTicket(ticket);
    }


    /**
     * 用户注册
     * @param user
     * @return
     */
    public Map<Object,Object> register(User user){
        HashMap<Object, Object> map = new HashMap<>();
        //一些判空判断
        if (user == null){
            throw new  IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号
        User u = userMapper.selectUserByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","该账号已存在！");
            return map;
        }
        //验证邮箱
        u = userMapper.selectUserByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","该邮箱已存在！");
            return map;
        }


        //注册用户
        user.setSalt(CommunityUtil.getRandomUUID().substring(0,5));
        //将将用户的密码进行二次加密 md5加密然后拼接上6个随机字符
        user.setPassword(CommunityUtil.md5(user.getPassword())+user.getSalt());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.getRandomUUID());
        //设置用户的默认头像
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        log.info("user ===>"+user.toString());
        //根据用户生成激活链接
        String url = domain + contextPath + "/activation/" + user.getId() + "/" +user.getActivationCode();

        context.setVariable("url",url);
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号",process);

        return map;
    }

    //激活码的业务方法
    public int activation(Integer userId,String code){
        //从数据库中获取
        User user = userMapper.selectUserById(userId);
        if (user!=null && user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)) {
            userMapper.updateUserStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILED;
        }
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param expiredTime : 过期时间 单位S
     * @return
     */
    public Map<String,Object> login(String username,String password,Integer expiredTime){
        Map<String,Object> map = new HashMap<>();
        //判空
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","账户密码不能为空");
            return map;
        }
        User user = userMapper.selectUserByName(username);
        //验证账号
        if (user==null){
            map.put("usernameMsg","该账户不存在，请注册");
            return map;
        }
        //验证状态
        if (user.getStatus()==ACTIVATION_FAILED){
            map.put("usernameMsg","该账户未激活,请激活账号");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password) + user.getSalt();
        if (!password.equals(user.getPassword())){
            map.put("passwordMsg","输入的密码不正确");
            return map;
        }
        //生成登录的凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.getRandomUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredTime*1000));
        //将凭证插入到数据库中
        loginTicketMapper.insertLoginTicket(loginTicket);
        //将登录的凭证放入到map中
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 登出
     * @param ticket :登录凭证
     */
    public void  logout(String ticket){
        loginTicketMapper.updateTicketStatus(ticket,1);
    }


    /**
     *
     * @param userId 用户id
     * @param headerUrl 用户头像url
     * @return
     */
    public int updateHeaderUrl(Integer userId,String headerUrl){
        return userMapper.updateUserHeadUrl(userId,headerUrl);
    }

}
