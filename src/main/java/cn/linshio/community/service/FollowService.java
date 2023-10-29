package cn.linshio.community.service;

import cn.linshio.community.entity.User;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

//关注业务类
@Service
public class FollowService implements CommunityConstant {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    //添加关注的对象
    public void follow(int userId,int entityType,int entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    //取消关注的对象
    public void unfollow(int userId,int entityType,int entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);
                return operations.exec();
            }
        });
    }

    //查询关注实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    //查询某用户关注的人
    public List<Map<String,Object>> findFollowee(int userId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds==null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            //查询用户
            User user = userService.selectUserById((Integer) targetId);
            map.put("user",user);
            //时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
    //查询某用户的粉丝
    public List<Map<String,Object>> findFollower(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds==null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            //查询用户
            User user = userService.selectUserById((Integer) targetId);
            map.put("user",user);
            //时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
