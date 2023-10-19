package cn.linshio.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    //生成随机的字符串 去除中间的-
    public static String getRandomUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    //md5加密
    public static String md5(String key){
        //StringUtils工具类进行判空
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
