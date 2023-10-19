import cn.linshio.community.MainApplication;
import cn.linshio.community.util.MailClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class MailTest {

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testTextSend(){
        mailClient.sendMail("17779664877@163.com","TestMail","Hello World");
    }

    @Test
    public void testHtmlSend(){
        Context context = new Context();
        context.setVariable("username","铃汐");
        String process = templateEngine.process("/mail/demo", context);
        log.info(process);
        mailClient.sendMail("17779664877@163.com","TestHTMLMail",process);
    }
}
