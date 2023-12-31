package cn.linshio.community.dao;

import cn.linshio.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//帖子dao
@Mapper
public interface DiscussPostMapper {
    //查询所有记录并 分页
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit,
                                         @Param("orderMode") int orderMode);

    //根据userId查询 该用户的帖子条数
    int selectDiscussPostRows(@Param("userId") int userId);

    //根据userId查询 该用户的帖子
    List<DiscussPost> selectDiscussPostsByUserId(@Param("userId") int userId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    //插入帖子
    int insertDiscussPost(DiscussPost discussPost);

    //根据id查询帖子
    DiscussPost selectDiscussPostById(@Param("id") int id);

    //更新评论的数量
    int updateCommentCount(@Param("id") int id,@Param("count") int count);

    //更新帖子的类型
    int updateType(@Param("id") int id,@Param("type") int type);

    //更新帖子的状态
    int updateStatus(@Param("id") int id,@Param("status") int status);

    //更新帖子的方法
    int updateScore(@Param("id") int id,@Param("score") double score);

}
