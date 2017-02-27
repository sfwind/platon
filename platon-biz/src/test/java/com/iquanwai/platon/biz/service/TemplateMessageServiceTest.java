package com.iquanwai.platon.biz.service;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by justin on 16/10/12.
 */
public class TemplateMessageServiceTest extends TestBase {
    @Autowired
    private TemplateMessageService templateMessageService;

    @Test
    public void testSend(){

    }
}
