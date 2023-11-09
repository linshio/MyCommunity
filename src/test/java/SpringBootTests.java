import cn.linshio.community.MainApplication;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;
    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    //一般用来初始化一些数据
    @Before
    public void before(){
        System.out.println("before");
        data = new DiscussPost();
        data.setTitle("Test");
        data.setContent("Test");
        data.setUserId(111);
        data.setType(0);
        data.setCreateTime(new Date());
        data.setStatus(0);
        data.setScore(0d);
        discussPostService.addDiscussPost(data);
    }

    //一般用来清理一些数据
    @After
    public void after(){
        System.out.println("after");
        discussPostService.updateStatus(data.getId(),2);
    }

    @Test
    public void test01(){
        System.out.println("test01");
    }
    @Test
    public void test02(){
        System.out.println("test02");
    }

    @Test
    public void testFindById(){
        DiscussPost discussPost = discussPostService.findDiscussById(data.getId());
        Assert.assertNotNull(discussPost);
        Assert.assertEquals(data.getTitle(),discussPost.getTitle());
        Assert.assertEquals(data.getContent(),discussPost.getContent());
    }

    @Test
    public void testUpdateScore(){
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1,rows);
        DiscussPost discussPost = discussPostService.findDiscussById(data.getId());
        Assert.assertEquals(2000.00,discussPost.getScore(),2);
    }

}
