package cn.linshio.community.util;

public interface CommunityConstant {
    //当前激活状态失败或者未激活
    int ACTIVATION_FAILED = 0;
    //当前激活的状态成功
    int ACTIVATION_SUCCESS = 1;
    //当前的激活的状态为重复激活
    int ACTIVATION_REPEAT = 2;

    //设置默认状态下的登录凭证的超时时间 12h
    int DEFAULT_EXPIRED_SECONDS = 3600*12;

    //设置记住状态下的登录凭证的超时时间 7Day
    int REMEMBER_EXPIRED_SECONDS = 3600*24*7;

    //实体类型：帖子
    int ENTITY_TYPE_POST = 1;
    //实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;
    //实体类型：用户
    int ENTITY_TYPE_USER = 3;

    //主题：评论
    String TOPIC_COMMENT = "comment";
    //主题：点赞
    String TOPIC_LIKE = "like";
    //主题：关注
    String TOPIC_FOLLOW = "follow";
    //主题：发帖
    String TOPIC_PUBLISH = "publish";

    //系统用户id
    int SYSTEM_USER_ID = 1;

}
