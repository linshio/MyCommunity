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
}
