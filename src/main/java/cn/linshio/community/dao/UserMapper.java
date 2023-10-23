package cn.linshio.community.dao;

import cn.linshio.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//用户dao
@Mapper
public interface UserMapper {
    //根据用户id 查询用户
    User selectUserById(@Param("id") int id);
    //根据用户名 查询用户
    User selectUserByName(@Param("username") String username);
    //根据邮箱   查询用户
    User selectUserByEmail(@Param("email") String email);
    //添加一个用户
    int insertUser(User user);

    //更新用户的状态  status
    int updateUserStatus(@Param("id") int id,
                         @Param("status") int status);
    //更新用户的头像链接
    int updateUserHeadUrl(@Param("id") int id,
                          @Param("headUrl") String headerUrl);
    //更新用户密码
    int updateUserPassword(@Param("id") int id,
                           @Param("password") String password);
}
