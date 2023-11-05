package cn.linshio.community.quartz;

import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.service.DiscussPostService;
import cn.linshio.community.service.ElasticsearchService;
import cn.linshio.community.service.LikeService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//帖子分数刷新的定时任务
@Slf4j
@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private LikeService likeService;

    @Resource
    private ElasticsearchService elasticsearchService;

    //成立日期
    public static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元时间失败"+e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        if (operations.size()==0){
            log.info("[任务取消] 没有要刷新的任务");
            return;
        }
        log.info("[任务开始] 正在刷新帖子的分数");
        while (operations.size()>0){
            this.refresh((Integer) operations.pop());
        }
        log.info("[任务结束] 帖子的分数已经刷新完毕");

    }

    //刷新该帖子的分数
    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussById(postId);
        if (post==null){
            log.error("该帖子不存在：id = "+postId);
            return;
        }

        //是否精华
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算分数
        double w = (wonderful?75:0)+commentCount* 10L +likeCount*2;
        double score = Math.log10(Math.max(w, 1)) + (post.getCreateTime().getTime() - epoch.getTime())/(1000*3600*24);

        //更新帖子的分数
        discussPostService.updateScore(postId,score);
        post.setScore(score);
        //同步搜索的数据
        elasticsearchService.saveDiscussPost(post);
    }
}
