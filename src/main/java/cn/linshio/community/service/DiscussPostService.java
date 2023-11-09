package cn.linshio.community.service;

import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-posts}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口：Cache, LoadingCache, AsyncLoadingCache

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    if (key == null || key.length()==0){
                        throw new IllegalArgumentException("参数错误！");
                    }
                    String[] params = key.split(":");
                    if (params==null || params.length!=2){
                        throw new IllegalArgumentException("参数错误！");
                    }
                    int offset = Integer.valueOf(params[0]);
                    int limit = Integer.valueOf(params[1]);
                    log.debug("load post list from DB");
                    return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                });
        //初始化帖子总数缓存
        postRowCache = Caffeine.newBuilder()
               .maximumSize(maxSize)
               .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
               .build(key -> {
                    log.debug("load post rows from DB");
                    return discussPostMapper.selectDiscussPostRows(key);
                });
    }

    //帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;
    //帖子总数的缓存
    private LoadingCache<Integer,Integer> postRowCache;


    public List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode){
        //根据页数与热帖的排行进行缓存
        if (userId==0 && orderMode ==1){
            return postListCache.get(offset+":"+limit);
        }
        log.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    //根据UserId返回该用户发表的帖子总数
    public int selectDiscussPostRows(int userId){
        if (userId==0){
            return postRowCache.get(userId);
        }
        log.debug("load post rows from DB");
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
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }

    //查询指定用户发布的帖子
    public List<DiscussPost> selectDiscussPostsByUserId(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPostsByUserId(userId,offset,limit);
    }
}
