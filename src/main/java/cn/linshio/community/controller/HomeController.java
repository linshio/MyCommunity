package cn.linshio.community.controller;

import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.entity.Page;
import cn.linshio.community.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主页模块
 */
@Controller
public class HomeController {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private UserMapper userMapper;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        //此处springMVC会自动将page实例化并放入到model中
        page.setRows(discussPostMapper.selectDiscussPostRows(0));
        page.setPath("/index");
        //将数据打包 将帖子与用户封装成一个集合，到时候便于访问
        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(0, page.getCurrentOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (posts!=null){
            for (DiscussPost discussPost : posts) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post",discussPost);
                User user = userMapper.selectUserById(discussPost.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "index";
    }
}
