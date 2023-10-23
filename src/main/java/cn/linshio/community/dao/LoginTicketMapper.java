package cn.linshio.community.dao;

import cn.linshio.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginTicketMapper {
    //插入一条登录凭证
    int insertLoginTicket(LoginTicket loginTicket);
    //根据凭证信息查找登录凭证
    LoginTicket selectLoginTicketByTicket(String ticket);
    //更新凭证状态
    int updateTicketStatus(@Param("ticket") String ticket,
                           @Param("status") int status);
}
