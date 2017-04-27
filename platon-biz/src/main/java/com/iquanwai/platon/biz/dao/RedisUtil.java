package com.iquanwai.platon.biz.dao;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by nethunder on 2017/4/26.
 */
@Repository
public class RedisUtil {
    private RedissonClient redissonClient;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    public RedissonClient getRedissonClient(){
        return this.redissonClient;
    }

    public String get(String key){
        return get(String.class, key);
    }

    public <T> T get(Class<T> tClass, String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

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
        logger.info("Thread {} will release the lock",Thread.currentThread().getId());
        lock.unlock();
        logger.info("Thread {} don't have the lock :{}", Thread.currentThread().getId(), lock.isHeldByCurrentThread());
    }
}
