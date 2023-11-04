import cn.linshio.community.MainApplication;
import cn.linshio.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

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

    @Test
    public void test(){
//        [uv:20231104,uv:20231105]
        List<String> list = new ArrayList<>();
        list.add("uv:20231104");
        list.add("uv:20231105");
        list.add("uv:20231106");


        System.out.println(Arrays.toString(list.toArray()));
    }
}
