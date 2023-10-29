import cn.linshio.community.MainApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.swing.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class RedisTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testMethod(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }

    @Test
    public void testHashMethod(){
        String redisKey = "test:hash";
        redisTemplate.opsForHash().put(redisKey,"name","张三");
        System.out.println(redisTemplate.opsForHash().get(redisKey,"name"));
    }

    @Test
    public void testListMethod(){
        String redisKey = "test:list";
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 1, 2));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
    }

    @Test
    public void testSetMethod(){
        String redisKey = "test:set";
        redisTemplate.opsForSet().add(redisKey,"A","B","C","D","E");
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        System.out.println(set.size(redisKey));
        System.out.println(set.pop(redisKey));
        System.out.println(set.members(redisKey));
    }

    @Test
    public void testZSetMethod(){
        String redisKey = "test:zset";
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(redisKey,"A",55);
        zset.add(redisKey,"B",77);
        zset.add(redisKey,"C",22);
        zset.add(redisKey,"D",33);
        System.out.println(zset.zCard(redisKey));
        System.out.println(zset.range(redisKey, 0, 2));
        System.out.println(zset.reverseRange(redisKey, 0, 2));
        System.out.println(zset.rank(redisKey, "A"));
        System.out.println(zset.reverseRank(redisKey, "B"));
        System.out.println(zset.score(redisKey, "A"));
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(redisKey);
        ops.increment();
        ops.increment(8);
        System.out.println(ops.get());
    }

    //编程式事务
    @Test
    public void testTransactional(){
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                //开启事务
                redisOperations.multi();

                SetOperations set = redisOperations.opsForSet();
                set.add( "001",  "zhangsan");
                set.add("002",  "lisi");
                set.add( "003","wangwu");

                System.out.println(set.members(redisKey));
                //提交事务
                return redisOperations.exec();
            }
        });
    }
}
