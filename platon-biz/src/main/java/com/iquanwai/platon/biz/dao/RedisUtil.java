package com.iquanwai.platon.biz.dao;

import com.alibaba.fastjson.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by nethunder on 2017/4/26.
 */
@Repository
public class RedisUtil {
    private RedissonClient redissonClient;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final long EXPIRED_TIME = 24 * 60 * 60;

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RedissonClient getRedissonClient() {
        return this.redissonClient;
    }

    /**
     * 默认获得字符串类型的数据
     *
     * @param key key
     */
    public String get(String key) {
        return get(String.class, key);
    }

    /**
     * 获取对象
     *
     * @param key   key
     * @param clazz class
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        return JSONObject.parseObject(json, clazz);
    }

    /**
     * 设置对象,可以直接设置字符串或者复杂类型
     *
     * @param key   key
     * @param value value
     */
    public void set(String key, Object value) {
        set(key, value, EXPIRED_TIME);
    }

    /**
     * 设置对象,可以直接设置字符串或者复杂类型
     *
     * @param key           key
     * @param value         value
     * @param timeToExpired 过期时间，单位秒
     */
    public void set(String key, Object value, Long timeToExpired) {
        Assert.notNull(key, "key 不能为null");
        Assert.notNull(value, "value 不能为null");
        RBucket<String> bucket = redissonClient.getBucket(key);
        String finalValue = null;
        // 如果是字符串就不用再转一次，如果是数字／对象都会经过fastjson转换为字符串
        if (value instanceof String) {
            finalValue = value.toString();
        } else {
            finalValue = JSONObject.toJSONString(value);
        }
        bucket.set(finalValue, timeToExpired == null ? EXPIRED_TIME : timeToExpired, TimeUnit.SECONDS);
    }

    /**
     * 同步锁
     *
     * @param key      锁
     * @param consumer 在锁里的操作
     */
    public void lock(String key, Consumer<RLock> consumer) {
        RLock lock = redissonClient.getLock(key);
        logger.info("Thread {} want the lock", Thread.currentThread().getId());
        try {
            lock.tryLock(60, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        logger.info("Thread {} has lock :{}", Thread.currentThread().getId(), lock.isHeldByCurrentThread());
        consumer.accept(lock);
        logger.info("Thread {} will release the lock", Thread.currentThread().getId());
        lock.unlock();
        logger.info("Thread {} don't have the lock :{}", Thread.currentThread().getId(), lock.isHeldByCurrentThread());
    }

    /**
     * 根据正则获取keys,默认每次向redis请求获取10行
     *
     * @param pattern 正则
     */
    public Iterable<String> getKeysByPattern(String pattern) {
        return redissonClient.getKeys().getKeysByPattern(pattern);
    }

    /**
     * 根据正则获取keys
     *
     * @param pattern 正则
     * @param count   每次向redis请求时最多获取多少行
     */
    public Iterable<String> getKeysByPattern(String pattern, Integer count) {
        return redissonClient.getKeys().getKeysByPattern(pattern, count);
    }


    /**
     * 通过正则删除数据
     *
     * @param pattern 正则
     * @return 删除的条数
     */
    public long deleteByPattern(String pattern) {
        return redissonClient.getKeys().deleteByPattern(pattern);
    }

    /**
     * 通过key删除数据
     *
     * @param key key
     * @return 删除的条数
     */
    public long deleteByKey(String key) {
        return redissonClient.getKeys().delete(key);
    }


    /**
     * 获取value <br/>
     * redisson的转换器会存储class信息，所以这个第一个参数只传String.class
     */
    private <T> T get(Class<T> tClass, String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }
}
