package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//登录凭证实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {
    private Integer id; //登录id
    private Integer userId; // 用户id
    private String ticket; // 凭证信息 核心数据
    private Integer status;//0-有效; 1-无效
    private Date expired; //登录的有效时间
}
