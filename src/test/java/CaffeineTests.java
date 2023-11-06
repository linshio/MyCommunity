import cn.linshio.community.MainApplication;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.OS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class CaffeineTests {

    @Resource
    private DiscussPostService postService;

    @Test
    public void initDataForTest(){
        for (int i = 0; i < 30000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("就业形势");
            post.setContent("今年互联网就业形势挺难的");
            post.setCreateTime(new Date());
            post.setScore(Math.random()*2000);
            postService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache(){
        System.out.println(postService.selectDiscussPosts(0,0,10,1));
        System.out.println(postService.selectDiscussPosts(0,0,10,1));
        System.out.println(postService.selectDiscussPosts(0,0,10,1));
        System.out.println(postService.selectDiscussPosts(0,0,10,0));
    }
}
