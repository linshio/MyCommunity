package cn.linshio.community.service;

import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int selectDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //添加一条帖子 需要对帖子进行信息的过滤 title 与 content
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost==null){
            throw new IllegalArgumentException("非参数异常，参数为空");
        }

        //将html进行转译
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词汇
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    //当插入一条评论的时候 就更新一下评论的数量
    public int updateCommentCount(int id,int count){
        return discussPostMapper.updateCommentCount(id,count);
    }


    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }
}
