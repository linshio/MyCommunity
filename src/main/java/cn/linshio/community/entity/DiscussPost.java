package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//帖子的实体类
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussPost {
    private Integer id;             // 帖子的id
    private Integer userId;          //发布该帖子的用户id
    private String title;           //帖子的标题
    private String content;         //帖子的内容
    private Integer type;           //帖子的类型  0-普通 1-置顶
    private Integer status;         //帖子的状态  0-正常 1-精华 2-拉黑
    private Date createTime;        //帖子的创建时间
    private Integer commentCount;   //帖子的评论数量
    private Double score;           //帖子的权重
}
