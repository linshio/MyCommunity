import cn.linshio.community.MainApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class QuartzTests {

    @Autowired
    private Scheduler scheduler;


    //删除job
    @Test
    public void testDeleteJob() throws SchedulerException {
        boolean b = scheduler.deleteJob(new JobKey("testJob", "testGroup"));
        System.out.println(b);
    }

}
