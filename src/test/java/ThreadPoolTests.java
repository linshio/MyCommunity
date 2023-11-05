import cn.linshio.community.MainApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class ThreadPoolTests {

    //JDK普通的线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    //JDK可以执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //spring普通的线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    //spring可以执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private void sleep(long m){
        try {
            Thread.sleep(m);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    //1.jdk普通线程池
    @Test
    public void testExecutorService(){
        Runnable task = () -> log.info("Hello 线程正在运行");
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
        sleep(10000);
    }

    //2.jdk定时线程池
    @Test
    public void testScheduledExecutorService(){
        Runnable task = () -> log.info("Hello testScheduledExecutorService 线程正在运行");
        scheduledExecutorService.scheduleAtFixedRate(task,10000,1000,TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    //3.spring普通的线程池
    @Test
    public void testTaskExecutor(){
        Runnable task = () -> log.info("Hello testTaskExecutor 线程正在运行");
        for (int i = 0; i < 10; i++) {
            taskExecutor.execute(task);
        }
        sleep(10000);
    }
    //4.spring定时线程池
    @Test
    public void testTaskScheduler(){
        Runnable task = () -> log.info("Hello testTaskScheduler 线程正在运行");
        taskScheduler.scheduleAtFixedRate(task,new Date(System.currentTimeMillis()+10000),1000);
        sleep(30000);
    }

}
