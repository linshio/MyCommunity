package cn.linshio.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    /**
     * 封装的json信息
     * @param code      状态码
     * @param message   携带的信息
     * @param map       对象
     * @return json
     */
    public static String getJSONString(int code, String message, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("message",message);
        if (map!=null){
            for (String key : map.keySet()) {
                jsonObject.put(key,map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String message){
        return getJSONString(code,message,null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }
}
