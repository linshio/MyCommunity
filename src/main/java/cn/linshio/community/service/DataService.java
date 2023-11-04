package cn.linshio.community.service;

import cn.linshio.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataService {

    @Resource
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    //将指定的ip计入uv
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    //统计指定日期范围内的uv
    public long calculateUV(Date startDate,Date endDate){
        if (startDate==null||endDate==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(startDate), dateFormat.format(endDate));
        String s = String.valueOf(keyList);
        Long union = redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        return size;
    }

    //将指定用户计入DAU
    public void recordDAU(int userId){
        String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    //统计指定日期范围内DAU
    public long calculateDAU(Date startDate,Date endDate){
        if (startDate==null||endDate==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        //实例化一个日期
        Calendar calendar = Calendar.getInstance();
        //设置日期的开始时间
        calendar.setTime(startDate);
        //当日期小于结束日期时，将日期加一天，继续循环
        while (!calendar.getTime().after(endDate)){
            String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(dauKey.getBytes());
            calendar.add(Calendar.DATE,1);//每次循环加上一天
        }

        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(startDate), dateFormat.format(endDate));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }

}
