package com.iquanwai.platon.biz.service;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Account;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 7/15/15.
 */
public class HelloServiceTest extends TestBase {
    @Autowired
    private InterlocutionService interlocutionService;
    @Autowired
    private AccountService accountService;

    @Test
    public void sendTest(){
        Account guestFromWeixin = accountService.getGuestFromWeixin("o-Es21bZakuqjBfVr7a-_j90WQuI", "ReboboXJ6IYwb62mc7cYbeRDr8G9tdfF1MCOonZFNghcO9EBdyFzPo-Q9juMPRjOkSbcMGddc1uprObNe3UznA");
        System.out.println(JSON.toJSONString(guestFromWeixin));
    }
}
