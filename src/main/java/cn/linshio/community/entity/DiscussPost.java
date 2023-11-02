package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

//帖子的实体类
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "discuss_post",type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {
    @Id
    private int id;             // 帖子的id
    @Field(type = FieldType.Integer)
    private int userId;          //发布该帖子的用户id

    //下面这两个是分词器analyzer存储分词器 searchAnalyzer搜索分词器
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;           //帖子的标题

    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;         //帖子的内容

    @Field(type = FieldType.Integer)
    private int type;           //帖子的类型  0-普通 1-置顶
    @Field(type = FieldType.Integer)
    private int status;         //帖子的状态  0-正常 1-精华 2-拉黑
    @Field(type = FieldType.Date)
    private Date createTime;        //帖子的创建时间
    @Field(type = FieldType.Integer)
    private int commentCount;   //帖子的评论数量
    @Field(type = FieldType.Double)
    private Double score;           //帖子的权重
}
