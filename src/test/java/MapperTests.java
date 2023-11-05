import cn.linshio.community.MainApplication;
import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.dao.LoginTicketMapper;
import cn.linshio.community.dao.MessageMapper;
import cn.linshio.community.dao.UserMapper;
import cn.linshio.community.entity.DiscussPost;
import cn.linshio.community.entity.LoginTicket;
import cn.linshio.community.entity.Message;
import cn.linshio.community.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
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

    @Resource
    private LoginTicketMapper loginTicketMapper;

    @Resource
    private MessageMapper messageMapper;

    @Test
    public void testSelectPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 1, 10,0);
        for (DiscussPost discussPost : discussPosts) {
            log.info(discussPost.toString());
        }

//        int rows = discussPostMapper.selectDiscussPostRows(0);
//        log.info("总共「{}」条",rows);
    }

    @Test
    public void testUserMapper(){
//        User testUser = new User("test", "test", "tt", "jb@qq.com", 0, 0, null, "http://www.bilibili.com", new Date());
//        int result = userMapper.insertUser(testUser);
//        log.info("插入「{}」条数据",result);
//        User user = userMapper.selectUserById(150);
//        User user = userMapper.selectUserByEmail("jb@qq.com");
//        int updateUserPassword = userMapper.updateUserPassword(150, "666666");
//        int updateUserStatus = userMapper.updateUserStatus(150, 1);
//        int updateUserHeadUrl = userMapper.updateUserHeadUrl(150, "http://linshio.cn");
//        User user = userMapper.selectUserByName("test");
//        log.info(user.toString()+"\n"+"updateUserPassword=>{} ,updateUserStatus=>{} ,updateUserHeadUrl=>{}",
//                updateUserPassword,updateUserStatus,updateUserHeadUrl);
//        userMapper.insertUser(testUser);
//        User user = userMapper.selectUserByName(testUser.getUsername());
//        log.info(String.valueOf(testUser.getId()));

    }

    @Test
    public void testTicketMapper(){
//        LoginTicket loginTicket = new LoginTicket(null,101,"az",0,
//                new Date(System.currentTimeMillis()+1000*60*60));//时间为一个小时
//        loginTicketMapper.insertLoginTicket(loginTicket);
        loginTicketMapper.updateTicketStatus("az",1);
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicketByTicket("az");
        log.info(loginTicket.toString());
    }

    @Test
    public void testMessageMapper(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectConversationsCount(111));//14

        for (Message letter : messageMapper.selectLetters("111_112", 0, 20)) {
            System.out.println(letter);
        }
        System.out.println(messageMapper.selectLettersCount("111_112"));
        System.out.println(messageMapper.selectLettersUnread(111, null));
        System.out.println(messageMapper.selectLettersUnread(111, "111_131"));

    }
}
