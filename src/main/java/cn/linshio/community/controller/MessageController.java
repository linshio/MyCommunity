package cn.linshio.community.controller;

import cn.linshio.community.entity.Message;
import cn.linshio.community.entity.Page;
import cn.linshio.community.entity.User;
import cn.linshio.community.service.MessageService;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class MessageController {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    //私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationsCount(user.getId()));

        //会话的列表 封装数据
        List<Message> messageList =
                messageService.findConversations(user.getId(), page.getCurrentOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (messageList!=null){
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLettersCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLettersUnread(user.getId(), message.getConversationId()));
                //对象用户 需要对象用户相关信息(头像)
                int targetUserId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                User targetUser = userService.selectUserById(targetUserId);
                map.put("targetUser",targetUser);
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        //查询未读消息总数量
        int lettersUnreadCount = messageService.findLettersUnread(user.getId(), null);
        model.addAttribute("lettersUnreadCount",lettersUnreadCount);
        return "/site/letter";
    }

    //todo:删除私信内容

    //私信的详情页
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,
                                  Page page, Model model){
        //分页
        page.setPath("/letter/detail/"+conversationId);
        page.setLimit(5);
        page.setRows(messageService.findLettersCount(conversationId));

        //私信的列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getCurrentOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList!=null){
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("formUser",userService.selectUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信的目标
        model.addAttribute("target",getLetterTarget(conversationId));
        //将消息置为已读
        List<Integer> letterIds = getLetterIds(letterList);
        if (!letterIds.isEmpty()){
            messageService.readMessage(letterIds);
        }
        return "/site/letter-detail";
    }

    //获取到当前用户的未读消息id
    private List<Integer> getLetterIds(List<Message> messages){
        ArrayList<Integer> list = new ArrayList<>();
        if (messages!=null){
            for (Message message : messages) {
                //当前用户为接收者的时候才会有是否读取 并且当前的消息为未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus()==0){
                    list.add(message.getId());
                }
            }
        }
        return list;
    }

    //获取发送人对象
    private User getLetterTarget(String conversationId){
        String[] s = conversationId.split("_");
        int id0 = Integer.parseInt(s[0]);
        int id1 = Integer.parseInt(s[1]);
        if (hostHolder.getUser().getId()==id0){
            return userService.selectUserById(id1);
        }else{
            return userService.selectUserById(id0);
        }
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.selectUserByName(toName);
        if (target==null){
            return CommunityUtil.getJSONString(400,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setConversationId(Math.min(message.getFromId(),message.getToId())+
                "_"+Math.max(message.getFromId(),message.getToId()));
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(200,"ok");
    }


}
