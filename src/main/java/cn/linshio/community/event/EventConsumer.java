package cn.linshio.community.event;

import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.entity.Event;
import cn.linshio.community.entity.Message;
import cn.linshio.community.service.DiscussPostService;
import cn.linshio.community.service.ElasticsearchService;
import cn.linshio.community.service.MessageService;
import cn.linshio.community.util.CommunityConstant;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//事件的消费者
@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

    @Resource
    private MessageService messageService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private ElasticsearchService elasticsearchService;

    //对用户点赞、关注、评论进行发送消息
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord<String,String> record){
        //数据校验
        if (record==null || record.value()==null){
            log.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event==null){
            log.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());

        //处理消息的内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        //如果event的map里面携带了数据就一起存到content中进行持久化
        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }

        //将消息内容进行持久化
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件，对用户发送的帖子转存到ES中
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishConsumer(ConsumerRecord<String,String> record){
        //数据校验
        if (record==null || record.value()==null){
            log.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event==null){
            log.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

}
