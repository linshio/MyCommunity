package cn.linshio.community.service;

import cn.linshio.community.dao.CommentMapper;
import cn.linshio.community.entity.Comment;
import cn.linshio.community.entity.ReplayData;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

//Comment评论业务类
@Service
public class CommentService implements CommunityConstant {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityId,int entityType,int offset,int limit){
        return commentMapper.selectCommentByEntity(entityId,entityType,offset,limit);
    }

    public int findCommentCount(int entityId,int entityType){
        return  commentMapper.selectCommentCount(entityId,entityType);
    }

    //查询我的回复
    public List<ReplayData> findCommentByUser(int userId,int offset,int limit){
        return commentMapper.selectCommentByUser(userId,offset,limit);
    }

    //查询我的回复条数
    public int findCommentUserRows(int userId){
        return commentMapper.selectCommentCountByUser(userId);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    /**
     * 事务的传播机制
     *REQUIRED:     支持当前事务，如果不存在就创建新事务
     *REQUIRES_NEW: 创建一个新事务，并且暂停当前事务
     *NESTED        如果当前存在事务，则嵌套在该事务中执行(具有独立的提交和回滚)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //对评论进行敏感词汇过滤以及进行 去除html影响
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子的数量 当前评论的类型如果为帖子
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            //当前评论的总条数
            int count = commentMapper.selectCommentCount(comment.getEntityId(), comment.getEntityType());
            //更新帖子的数量
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }


}
