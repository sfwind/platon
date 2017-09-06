package com.iquanwai.platon.web.fragmentation.controller.bible;

import com.iquanwai.platon.biz.domain.bible.SubscribeArticleService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.po.bible.SubscribePointCompare;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/9/6.
 */
@RestController
@RequestMapping("/rise/bible")
public class BibleController {
    private Logger Logger = LoggerFactory.getLogger(this.getClass());

    public static Integer PAGE_SIZE = 20;

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private SubscribeArticleService subscribeArticleService;


    @RequestMapping("/load/article/{pageId}")
    public ResponseEntity<Map<String, Object>> loadArticleGroup(LoginUser loginUser, @PathVariable Integer pageId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章列表")
                .action("加载");
        operationLogService.log(operationLog);
        Page page = new Page();
        page.setPageSize(PAGE_SIZE * pageId);
        List<SubscribeArticle> subscribeArticles = subscribeArticleService.loadSubscribeArticleList(loginUser.getId(), page);
        BibleRefreshListDto<SubscribeArticle> result = new BibleRefreshListDto<>();
        result.setList(subscribeArticles);
        result.setEnd(page.isLastPage());
        // 查看是否firstOpen
        return WebUtils.result(result);
    }

    @RequestMapping("/load/article")
    public ResponseEntity<Map<String, Object>> loadArticleGroup(LoginUser loginUser, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章列表")
                .action("加载");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(PAGE_SIZE);
        List<SubscribeArticle> subscribeArticles = subscribeArticleService.loadSubscribeArticleList(loginUser.getId(), page);
        BibleRefreshListDto<SubscribeArticle> result = new BibleRefreshListDto<>();
        result.setList(subscribeArticles);
        result.setEnd(page.isLastPage());
        return WebUtils.result(result);
    }

    @RequestMapping("/favor/{articleId}")
    public ResponseEntity<Map<String, Object>> favorArticle(LoginUser loginUser, @PathVariable Integer articleId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章")
                .action("喜欢");
        operationLogService.log(operationLog);
        Boolean result = subscribeArticleService.favorArticle(loginUser.getId(), articleId);
        return WebUtils.result(result);
    }

    @RequestMapping("/open/{articleId}")
    public ResponseEntity<Map<String, Object>> openArticle(LoginUser loginUser, @PathVariable Integer articleId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章")
                .action("浏览");
        operationLogService.log(operationLog);
        Boolean result = subscribeArticleService.viewArticle(loginUser.getId(), articleId);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/load/score", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadScore(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("分数")
                .action("获取");
        operationLogService.log(operationLog);
        List<SubscribePointCompare> compareList = subscribeArticleService.loadSubscribeViewPointList(loginUser.getId());
        return WebUtils.result(compareList);
    }
}
