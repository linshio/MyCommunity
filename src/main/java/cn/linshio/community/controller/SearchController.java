package cn.linshio.community.controller;

import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.entity.Page;
import cn.linshio.community.service.ElasticsearchService;
import cn.linshio.community.service.LikeService;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class SearchController implements CommunityConstant {

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    //搜索帖子 keyWord通过keyWord?=xxx传参
    @GetMapping("/search")
    public String search(String keyWord, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> discussPosts =
                elasticsearchService.searchDiscussPost(keyWord, page.getCurrent()-1, page.getLimit());
        //封装数据
        List<Map<String,Object>> list = new ArrayList<>();
        if (discussPosts!=null){
            for (DiscussPost post : discussPosts) {
                Map<String,Object> map = new HashMap<>();
                //封装帖子
                map.put("post",post);
                //封装作者
                map.put("user",userService.selectUserById(post.getUserId()));
                //封装点赞数
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                list.add(map);
            }
        }
        model.addAttribute("discussPosts",list);
        model.addAttribute("keyWord",keyWord);

        //分页信息
        page.setPath("/search?keyWord="+keyWord);
        page.setRows(discussPosts == null ? 0 : (int) discussPosts.getTotalElements());
        return "site/search";
    }

}
