package cn.linshio.community.service;

import cn.linshio.community.dao.LoginTicketMapper;
import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.LoginTicket;
import cn.linshio.community.entity.User;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.MailClient;
import cn.linshio.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;


    //根据用户名查询用户
    public User selectUserByName(String username){
        return userMapper.selectUserByName(username);
    }

    //查询凭证
    public LoginTicket selectLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return  (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * 用户注册
     * @param user 用户对象
     * @return map集合收集的错误信息
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
        //将将用户的密码进行二次加密 md5拼接上6个随机字符然后加密
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
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
        //根据用户生成激活链接
        String url = domain + contextPath + "/activation/" + user.getId() + "/" +user.getActivationCode();
        context.setVariable("url",url);
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号",process);
        return map;
    }

    //激活码的业务方法
    public int activation(int userId,String code){
        //从数据库中获取
        User user = userMapper.selectUserById(userId);
        if (user!=null && user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)) {
            userMapper.updateUserStatus(userId,1);
            //清楚用户的缓存
            clearCacheUser(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILED;
        }
    }

    /**
     * 用户登录
     * @param username 账户名
     * @param password 账户密码
     * @param expiredTime : 过期时间 单位S
     * @return map集合收集的错误信息
     */
    public Map<String,Object> login(String username,String password,int expiredTime){
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
        password = CommunityUtil.md5(password + user.getSalt()) ;
        if (!password.equals(user.getPassword())){
            map.put("passwordMsg","输入的密码不正确");
            return map;
        }
        //生成登录的凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.getRandomUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredTime* 1000L));
        //将凭证插入到数据库中
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        //将登录的凭证放入到map中
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 登出
     * @param ticket :登录凭证
     */
    public void  logout(String ticket){
//        loginTicketMapper.updateTicketStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        //将用户状态改为1然后在存回去
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }


    /**
     *
     * @param userId 用户id
     * @param headerUrl 用户头像url
     * @return
     */
    public int updateHeaderUrl(int userId,String headerUrl){
        int rows = userMapper.updateUserHeadUrl(userId, headerUrl);
        clearCacheUser(userId);
        return rows;
    }

    //查询用户
    public User selectUserById(int id){
//        return userMapper.selectUserById(id);
        User user = getCacheUser(id);
        if (user==null){
            user = initCacheUser(id);
        }
        return user;
    }

    //优先从缓存中去取值
    private User getCacheUser(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
       return (User) redisTemplate.opsForValue().get(userKey);
    }
    //如果取不到就去数据库中找
    private User initCacheUser(int userId){
        User user = userMapper.selectUserById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        //用户一个小时的缓存
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * @param userId
     */
    //当数据进行变更的时候就清楚缓存的数据
    private void clearCacheUser(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    //返回用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.selectUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add((GrantedAuthority) () -> {
            switch (user.getType()){
                case 1:
                    return AUTHORITY_ADMIN;
                case 2:
                    return AUTHORITY_MODERATOR;
                default:
                    return AUTHORITY_USER;
            }
        });
        return list;
    }

    // 重置密码
    public Map<String,Object> resetPassword(String email,String password){
        Map<String, Object> map = new HashMap<>();
        //进行空值处理
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证邮箱
        User user = userMapper.selectUserByEmail(email);
        if (user==null){
            map.put("emailMsg","该邮箱还没有进行注册");
            return map;
        }

        //重制密码
        //进行加密
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updateUserPassword(user.getId(),password);
        map.put("user",user);
        return map;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectUserById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updateUserPassword(userId, newPassword);

        return map;
    }
}
