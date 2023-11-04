import cn.linshio.community.MainApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.Random;

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

    //统计2k个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 1000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        System.out.println("==============");
        for (int i = 1; i <= 1000; i++) {
            int r =  new Random().nextInt(1000) +1;
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }


    //合并HyperLogLog数据
    @Test
    public void testHyperLogLogUnion(){
//        String redisKey1 = "test:hll:02";
//        for (int i = 1; i <= 100; i++) {
//            redisTemplate.opsForHyperLogLog().add(redisKey1, i);
//        }
//        String redisKey2 = "test:hll:03";
//        for (int i = 51; i <= 150; i++) {
//            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
//        }
//        String redisKey3 = "test:hll:04";
//        for (int i = 101; i <= 200; i++) {
//            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
//        }
//
//        String unionKey = "test:hll:union";
//        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey1, redisKey2, redisKey3);

//        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
        System.out.println(redisTemplate.opsForHyperLogLog().add("mytest:01", 2));
        System.out.println(redisTemplate.opsForHyperLogLog().size("mytest:01"));
    }

    //统计一组数据的bool值
    @Test
    public void testBitMap(){
        String redisKey = "test:bitmap:01";
        redisTemplate.opsForValue().setBit(redisKey, 0, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 6, true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        //统计
        Object object = redisTemplate.execute((RedisCallback<Object>) redisConnection -> redisConnection.bitCount(redisKey.getBytes()));
        System.out.println(object);
    }

    //统计三组数据的bool值,并对这三组数据进行or运算
    @Test
    public void  testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);
        redisTemplate.opsForValue().setBit(redisKey2, 5, true);
        redisTemplate.opsForValue().setBit(redisKey2, 6, true);

        String redisKey = "test:bm:or";
        Object object = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes()
                        , redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(object);
    }

}
