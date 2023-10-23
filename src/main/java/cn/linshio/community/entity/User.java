package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//用户实体类
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;         //用户id
    private String username;    //用户名
    private String password;    //密码
    private String salt;        //加密密码随机字符串 拼接在用户的密码后面
    private String email;       //用户邮箱
    private int type;       //用户类型 0-普通用户 1-超级管理员 2-版主
    private int status;     //用户状态 0-未激活 1-已激活
    private String activationCode;//激活码
    private String headerUrl;   //用户头像的访问路径
    private Date createTime;    //用户创建时间
}
