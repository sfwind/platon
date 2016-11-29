package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.weixin.pay.PayService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/9/16.
 */
public class PayServiceTest extends TestBase {
    @Autowired
    private PayService payService;

    @Test
    public void testClosePay(){
        payService.closeOrder();
    }
}
