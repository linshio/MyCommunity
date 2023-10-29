package cn.linshio.community.controller;

import cn.linshio.community.entity.Page;
import cn.linshio.community.entity.User;
import cn.linshio.community.service.FollowService;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Resource
    private FollowService followService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(200,"已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(200,"已取消关注");
    }

    //查询当前的用户关注的用户并展示
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        //查询当前的用户
        User user = userService.selectUserById(userId);
        if (user==null){
            throw new RuntimeException("当前用户不存在");
        }
        model.addAttribute("user",user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        //service数据处理
        List<Map<String, Object>> userList = followService.findFollowee(userId, page.getCurrentOffset(), page.getLimit());
        if (userList!=null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    //查询当前的用户的粉丝并展示
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        //查询当前的用户
        User user = userService.selectUserById(userId);
        if (user==null){
            throw new RuntimeException("当前用户不存在");
        }
        model.addAttribute("user",user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        //service数据处理
        List<Map<String, Object>> userList = followService.findFollower(userId, page.getCurrentOffset(), page.getLimit());
        if (userList!=null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    //判断当前用户是否关注了传入参数的用户
    private boolean hasFollowed(int userId){
        //当前用户
        User user = hostHolder.getUser();
        //当前用户为空的话就默认没有关注
        if (user==null){
            return false;
        }
        return followService.hasFollowed(user.getId(),ENTITY_TYPE_USER,userId);
    }

}
