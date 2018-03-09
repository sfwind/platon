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
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser("o-Es21RVF3WCFQMOtl07Di_O9NVo");

        templateMessage.setTemplate_id(ConfigUtils.courseCloseMsg());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setUrl("https://www.confucius.mobi/rise/static/course/schedule/plan");
        templateMessage.setData(data);
        String first = "记住这个号码：111；你是这个号码学员的天使哦！";
        String remark = "对了，课程结束前，不要互相交流号码信息~\n还没加群？点击查看群二维码。";
        data.put("first", new TemplateMessage.Keyword(first));
        data.put("keyword1", new TemplateMessage.Keyword("结构化思维明天开始"));
        data.put("keyword2", new TemplateMessage.Keyword("明天凌晨"));
        data.put("remark", new TemplateMessage.Keyword(remark));
        templateMessageService.sendMessage(templateMessage);
    }
}
