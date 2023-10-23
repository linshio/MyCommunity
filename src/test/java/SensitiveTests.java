import cn.linshio.community.MainApplication;
import cn.linshio.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class SensitiveTests {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitive(){
        String test = "我们可以吸毒，嫖娼等等。。。";
        log.info(sensitiveFilter.filter(test));

    }
}
