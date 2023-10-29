package cn.linshio.community.util;

//redis主键工具类
public class RedisKeyUtil {
    //key的分隔符
    public static final String SPLIT = ":";
    //entity key的前缀
    public static final String PREFIX_ENTITY_LIKE = "like:entity";
    //user key的前缀
    public static final String PREFIX_USER_LIKE = "like:user";

    //我关注的对象
    public static final String PREFIX_FOLLOWEE = "followee";
    //关注我的粉丝
    public static final String PREFIX_FOLLOWER = "follower";

    //验证码
    public static final String PREFIX_KAPTCHA = "kaptcha";

    //登录凭证
    public static final String PREFIX_TICKET = "ticket";

    //用户
    public static final String PREFIX_USER = "user";

    /**
     *某个评论或帖子的赞 例：like:entity:user:2  存set
     * @param entityType 点赞对象的类型
     * @param entityId   点赞对象的id
     * @return key
     */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT +entityType + SPLIT + entityId;
    }

    /**
     *
     * @param userId 用户id
     * @return key
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体 followee:userId:entityType ->zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT +userId +SPLIT +entityType;
    }

    //某个实体拥有的粉丝 follower:entityType:entityId ->zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT +entityType +SPLIT +entityId;
    }

    //登录验证码 参数为临时的凭证
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录的凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户信息
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

}
