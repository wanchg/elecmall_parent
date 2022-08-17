package com.atguigu.gmall.common.cache;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.spring.web.json.Json;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object = null;
        /*
        * 1.获取方法上的注解
        * 2.获取到注解的前缀，并组成缓存的key
        * 3.根据key获取缓存中的数据
        * 4.判断是否获取到了数据
        * */

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取到注解的前缀
        String prefix = gmallCache.prefix();
        //获取到方法上的参数
        Object[] args = joinPoint.getArgs();
        //组成缓存的key
       String key = prefix + Arrays.asList(args);
        try {
            //从缓存中获取方法
            object = gitCache(key,signature);
            //判断
            if (object == null){
                //分布式锁的逻辑
                RLock rLock = redissonClient.getLock(key + ":lock");
                boolean status = rLock.tryLock(100, 10, TimeUnit.SECONDS);

                if (status){
                    try {
                        //查询数据库,执行被注解标识的代码快
                        object = joinPoint.proceed(joinPoint.getArgs());
                        //判断，防止缓存击穿
                        if (object == null){
                            //opsForValue  需要的是字符串，
                            Object obj = new Object();
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(obj),10,TimeUnit.SECONDS);
                            return obj;
                        }

                        //如果不为空
                        redisTemplate.opsForValue().set(key,JSON.toJSONString(object), RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return object;
                    }finally {
                        rLock.unlock();
                    }
                }else {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //自旋
                    cacheAroundAdvice(joinPoint);
                }

            }else {
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return joinPoint.proceed(joinPoint.getArgs());
    }

    private Object gitCache(String key,MethodSignature signature) {
        String sObject = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(sObject)){
            Class returnType = signature.getReturnType();
            //将字符串变为要返回的类型
            return JSON.parseObject(sObject,returnType);
        }
        return null;
    }
}
