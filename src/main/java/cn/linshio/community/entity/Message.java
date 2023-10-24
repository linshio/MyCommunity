package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//私信消息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int id;                 //id
    private int fromId;             //发送方id
    private int toId;               //接收方id
    private String conversationId;  //消息组合id
    private String content;         //消息内容
    private int status;             //消息状态 0-未读 1-已读 2-删除
    private Date createTime;        //创建时间
}
