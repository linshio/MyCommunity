package cn.linshio.community.service;

import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.entity.DiscussPost;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> selectDiscussPosts(Integer userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int selectDiscussPostRows(Integer userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
