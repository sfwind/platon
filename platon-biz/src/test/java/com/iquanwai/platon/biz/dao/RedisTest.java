package com.iquanwai.platon.biz.dao;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by nethunder on 2017/4/26.
 */
public class RedisTest extends TestBase {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test(){

//        AccessToken token = new AccessToken();
//        token.setAccessToken("fwefewfew");
//        redisUtil.set("accessToken", token);
        log(redisUtil.get("accessToken"));
    }

    @Test
    public void setTest() throws InterruptedException {
//        redisUtil.set("a", "gsegsdfsdf");
//        log(redisUtil.get(String.class, "a"));
//        Profile profile = new Profile();
//        profile.setNickname("薛定谔的猫");
//
//        redisUtil.set("user:xue", profile);

//        log(redisUtil.get(Profile.class, "user:xue").getNickname());

//        log(redisUtil.get(Profile.class, "user:xue").getNickname());

//        redisUtil.set("a", "fwefewf");

//        log(redisUtil.get(String.class, "a"));

        new Thread(()->{
            redisUtil.lock("flag", lock -> {
                Map<String, Object> map = Maps.newHashMap();
                log("a 加锁");
                map.get("test").getClass();
            });
        }).start();

        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisUtil.lock("flag",lock -> {
                log("b 加锁");
            });
        }).start();


        Thread.sleep(10000);

    }
}
