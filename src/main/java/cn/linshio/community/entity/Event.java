package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

//封装的事件
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    //事件的主题
    private String topic;
    //事件触发的人
    private int userId;
    //作用于的实体类型
    private int entityType;
    //作用于的实体id
    private int entityId;
    //作用于的实体作者的id
    private int entityUserId;
    //一些可以具有扩展性的事件
    private Map<String,Object> data = new HashMap<>();
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }

}
