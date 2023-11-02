package cn.linshio.community.dao;

import cn.linshio.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //根据类型查找评论
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId,
                                        @Param("entityType") int entityType,
                                        @Param("offset") int offset, @Param("limit") int limit);
    //根据类型查找评论的总条数
    int selectCommentCount(@Param("entityId") int entityId,
                           @Param("entityType") int entityType);

    //添加一条评论
    int insertComment(Comment comment);

    //根据id查询comment
    Comment selectCommentById(int id);
}
