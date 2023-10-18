package cn.linshio.community.service;

import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public User selectUserById(Integer id){
        return userMapper.selectUserById(id);
    }
}
