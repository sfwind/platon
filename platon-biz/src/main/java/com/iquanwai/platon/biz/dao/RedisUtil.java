package com.iquanwai.platon.biz.dao;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by nethunder on 2017/4/26.
 */
@Repository
public class RedisUtil {
    private RedissonClient redissonClient;

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
}
