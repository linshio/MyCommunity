package cn.linshio.community.service;

import cn.linshio.community.dao.MessageMapper;
import cn.linshio.community.entity.Message;
import cn.linshio.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationsCount(int userId){
        return messageMapper.selectConversationsCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset, int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLettersCount(String conversationId){
        return messageMapper.selectLettersCount(conversationId);
    }

    public int findLettersUnread(int userId,String conversationId){
        return messageMapper.selectLettersUnread(userId,conversationId);
    }

    //添加一条消息
    public int addMessage(Message message){
        //敏感词汇过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    //修改消息的状态为已读
    public int readMessage(List<Integer> ids){
        //1-状态已读 0-状态未读
        return messageMapper.updateStatus(ids,1);
    }

}
