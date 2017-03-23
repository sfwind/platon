package com.iquanwai.platon.biz.dao;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScoreDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.po.common.Account;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by justin on 17/2/12.
 */
public class FollowUsersDaoTest extends TestBase {
    @Autowired
    public FollowUserDao followUserDao;

    @Autowired
    private ProblemScoreDao problemScoreDao;
    @Test
    public void queryAccounts(){
        List<String> openids = Lists.newArrayList();
        openids.add("o5h6ywlXxHLmoGrLzH9Nt7uyoHbM");
        List<Account> accounts = followUserDao.queryAccounts(openids);
    }
    @Test
    public void batchInsertTest(){
        System.out.println(problemScoreDao.userPorblemScoreCount("fsdfsdf", 1));
    }
}
