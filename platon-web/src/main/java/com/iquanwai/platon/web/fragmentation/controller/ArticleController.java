package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.richtext.RichTextService;
import com.iquanwai.platon.biz.po.RichText;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by 三十文
 */
@Api("富文本相关")
@RestController
@RequestMapping("/rise/article")
public class ArticleController {

    @Autowired
    private RichTextService richTextService;

    @ApiOperation("获取富文本")
    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRichText(@RequestParam("id") Integer textId) {
        RichText richText = richTextService.loadRichText(textId);
        if (richText != null) {
            return WebUtils.result(richText);
        } else {
            return WebUtils.error("当前文章不存在");
        }
    }

}
