package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//评论实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private int id;         //评论id
    private int userId;     //评论用户id
    private int entityType; //评论对象类型 1-帖子 2-评论
    private int entityId;   //评论对象id
    private int targetId;   //回复的目标
    private String content; //评论的内容
    private int status;     //评论状态 0-正常
    private Date createTime;// 创建时间
}
