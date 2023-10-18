package cn.linshio.community.dao;

import cn.linshio.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//帖子dao
@Mapper
public interface DiscussPostMapper {
    //查询所有记录并 分页
    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    //根据userId查询 该用户的帖子条数
    int selectDiscussPostRows(@Param("userId") Integer userId);

}
