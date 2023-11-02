package cn.linshio.community.dao;

import cn.linshio.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话返回一条最新的私信 也就是id值最大的
    List<Message> selectConversations(@Param("userId")int userId,@Param("offset") int offset,@Param("limit") int limit);
    //查询当前用户的会话数量
    int selectConversationsCount(@Param("userId") int userId);
    //查询某个会话的私信列表
    List<Message> selectLetters(@Param("conversationId")String conversationId,
                                @Param("offset") int offset,@Param("limit") int limit);
    //查询某个会话的私信数量
    int selectLettersCount(@Param("conversationId")String conversationId);
    //查询未读私信的总数量 或会话的总数量
    int selectLettersUnread(@Param("userId") int userId,
                            @Param("conversationId") String conversationId);
    //添加一条私信
    int insertMessage(Message message);

    //修改状态的方法
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    //查询某个主题下最新的通知
    Message selectLatestNotice(@Param("userId") int userId,@Param("topic") String topic);
    //查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId") int userId,@Param("topic") String topic);
    //查询未读的通知数量
    int selectUnreadNoticeCount(@Param("userId") int userId,@Param("topic") String topic);

    //查询某个主题所包含的通知列表
    List<Message> selectNotices(@Param("userId") int userId,@Param("topic") String topic,
                                @Param("offset") int offset,@Param("limit") int limit);

}
