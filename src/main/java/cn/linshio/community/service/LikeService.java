package cn.linshio.community.service;

import cn.linshio.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


//点赞业务类
@Service
public class LikeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    //点赞方法
    public void like(int userId,int entityType,int entityId,int entityUserId){
        //启用redis事务的功能
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String entityUserKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean member = operations.opsForSet().isMember(entityLikeKey, userId);
                //启用事务
                redisTemplate.multi();
                //如果已经点过了赞就取消点赞
                if (member){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    //点赞总数-1
                    operations.opsForValue().decrement(entityUserKey);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    //点赞总数-1
                    operations.opsForValue().increment(entityUserKey);
                }
                //执行事务
                return redisTemplate.exec();
            }
        });

    }

    //查询实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //如果查询出已经点过赞就返回1，否则返回0
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId){
        String entityUserKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(entityUserKey);
        return count==null?0: count;
    }
}
