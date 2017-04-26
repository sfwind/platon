package com.iquanwai.platon.biz.dao;

import com.iquanwai.platon.biz.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    public void setTest(){
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

        redisUtil.lock("flag", lock -> {
            System.out.println(Thread.currentThread().getId() + " is lock:" + lock.isHeldByCurrentThread());
        });



    }
}
