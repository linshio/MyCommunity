package cn.linshio.community.controller;

import cn.linshio.community.entity.*;
import cn.linshio.community.event.EventProducer;
import cn.linshio.community.service.CommentService;
import cn.linshio.community.service.DiscussPostService;
import cn.linshio.community.service.LikeService;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @Resource
    private EventProducer eventProducer;

    /**
     * 添加一条帖子
     * @param title     帖子的标题
     * @param content   帖子的内容
     * @return json
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if (user==null){
            return CommunityUtil.getJSONString(403,"用户不存在，请先登录");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setScore(0d);
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //触发发帖的事件 目的是给到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.findEvent(event);

        return CommunityUtil.getJSONString(200,"帖子发布成功");
    }


    //查询帖子
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId")int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussById(discussPostId);
        model.addAttribute("discussPost",discussPost);
        //作者
        User user = userService.selectUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //帖子点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        //帖子点赞状态
        int likeStatus = hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);

        //评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //查询评论
        List<Comment> commentList = commentService.findCommentByEntity(discussPost.getId(), ENTITY_TYPE_POST,
                page.getCurrentOffset(), page.getLimit());
        //封装数据 评论列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList!=null){
            for (Comment comment : commentList) {
                //评论vo
                HashMap<String, Object> commentVo = new HashMap<>();
                //当前评论的对象
                commentVo.put("comment",comment);
                //当前评论的用户，也就是作者
                commentVo.put("user",userService.selectUserById(comment.getUserId()));

                //评论点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                //评论点赞状态
                likeStatus = hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);

                //回复的列表
                List<Comment> replyList = commentService.findCommentByEntity(comment.getId(), ENTITY_TYPE_COMMENT,
                        0, Integer.MAX_VALUE);
                //回复的vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList!=null){
                    for (Comment reply : replyList) {
                        //回复vo
                        HashMap<String, Object> replyVo = new HashMap<>();
                        //回复对象
                        replyVo.put("reply",reply);
                        //回复的作者
                        replyVo.put("user",userService.selectUserById(reply.getUserId()));
                        //回复的目标，也就是回复的对象
                        User target = reply.getTargetId()==0?null:userService.selectUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        //评论点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        //评论点赞状态
                        likeStatus = hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);

                        //将map装入集合中
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复的数量
                int replyCount = commentService.findCommentCount(comment.getId(), ENTITY_TYPE_COMMENT);
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }




}
