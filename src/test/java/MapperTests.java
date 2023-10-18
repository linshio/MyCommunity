import cn.linshio.community.MainApplication;
import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class MapperTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Resource
    private UserMapper userMapper;

    @Test
    public void testSelectPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 1, 10);
        for (DiscussPost discussPost : discussPosts) {
            log.info(discussPost.toString());
        }

//        int rows = discussPostMapper.selectDiscussPostRows(0);
//        log.info("总共「{}」条",rows);
    }

    @Test
    public void testUserMapper(){
//        User testUser = new User(null, "test", "test", "tt", "jb@qq.com", 0, 0, null, "http://www.bilibili.com", new Date());
//        int result = userMapper.insertUser(testUser);
//        log.info("插入「{}」条数据",result);
//        User user = userMapper.selectUserById(150);
//        User user = userMapper.selectUserByEmail("jb@qq.com");
        int updateUserPassword = userMapper.updateUserPassword(150, "666666");
        int updateUserStatus = userMapper.updateUserStatus(150, 1);
        int updateUserHeadUrl = userMapper.updateUserHeadUrl(150, "http://linshio.cn");
        User user = userMapper.selectUserByName("test");
        log.info(user.toString()+"\n"+"updateUserPassword=>{} ,updateUserStatus=>{} ,updateUserHeadUrl=>{}",
                updateUserPassword,updateUserStatus,updateUserHeadUrl);
    }

}
