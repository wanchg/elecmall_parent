package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.common.config.RedissonConfig;
import com.atguigu.gmall.service.TestService;
import org.apache.commons.lang.math.RandomUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    public String readLock(){
        //创建对象
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        //上锁
        lock.readLock().lock(10,TimeUnit.SECONDS);
        String meg = redisTemplate.opsForValue().get("meg");
        return meg;
    }

    public String writeLock(){
        //创建对象
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        lock.writeLock().lock(10,TimeUnit.SECONDS);
        String string = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("meg","123");

        return string;
    }

    @Override
    public void testRedis() throws InterruptedException {
        RLock lock = redissonClient.getLock("lock");
        //lock.lock(10,TimeUnit.SECONDS);

        //最多等待100秒，上锁以后10秒自动解锁
        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        if (res){
            //上锁成功，执行业务代码
            try {
                //执行工作
                String num = redisTemplate.opsForValue().get("num");
                if (StringUtils.isEmpty(num)){
                    return;
                }
                //操作
                int intNum = Integer.parseInt(num);

                redisTemplate.opsForValue().set("num",String.valueOf(++intNum));
            } finally {
                lock.unlock();
            }
        }else {
            lock.unlock();
        }

    }



    /*@Override
    public void testRedis() {
        //生成一个随机数
        String string = UUID.randomUUID().toString();
        //加redis锁
        Boolean status = redisTemplate.opsForValue().setIfAbsent("lock", string,3L, TimeUnit.SECONDS);
        if (status){
            //获取数据
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(num)){
                return;
            }
            int intNum = Integer.parseInt(num);

            //操作
            redisTemplate.opsForValue().set("num",String.valueOf(++intNum));

            *//*if (redisTemplate.opsForValue().get("lock").equals(string)){
                //释放锁
                redisTemplate.delete("lock");
            }*//*

            //添加lua脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //  创建对象
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
            //  设置lua脚本
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            //  redis 调用LUA 脚本
            redisTemplate.execute(redisScript, Arrays.asList("lock"),string);

        }else {
            try {
                Thread.sleep(300);
                this.testRedis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/
}
