package cn.linshio.community.event;


import cn.linshio.community.entity.Event;
import com.alibaba.fastjson.JSONObject;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//事件的生产者
@Component
public class EventProducer {

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;

    //处理事件
    public void findEvent(Event event){
        //将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
