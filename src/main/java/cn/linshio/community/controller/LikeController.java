package cn.linshio.community.controller;

import cn.linshio.community.entity.User;
import cn.linshio.community.service.LikeService;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;

//处理点赞
@Controller
public class LikeController {

    @Resource
    private LikeService likeService;

    @Resource
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        //后期会统一处理权限问题
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //点赞数量
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        //点赞状态
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //封装返回的结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",entityLikeCount);
        map.put("likeStatus",entityLikeStatus);
        return CommunityUtil.getJSONString(200,null,map);
    }
}
