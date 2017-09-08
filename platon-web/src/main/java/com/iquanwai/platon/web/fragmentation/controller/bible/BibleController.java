package com.iquanwai.platon.web.fragmentation.controller.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.bible.SubscribeArticleService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.po.bible.SubscribePointCompare;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/load/article/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadArticleGroup(LoginUser loginUser, @PathVariable(value = "date") String dateStr, @RequestParam("pageId") Integer pageId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章列表")
                .action("加载");
        operationLogService.log(operationLog);
        Page page = new Page();
        page.setPageSize(PAGE_SIZE * pageId);
        List<SubscribeArticle> subscribeArticles = subscribeArticleService.loadSubscribeArticleListToCertainDate(loginUser.getId(), page, dateStr);
        // 分天
        List<DailyArticleDto> dailyArticleGroup = Lists.newArrayList();
        Map<Date, List<SubscribeArticle>> dateListMap = subscribeArticles.stream().collect(Collectors.groupingBy(SubscribeArticle::getUpTime));
        dateListMap.forEach((date, subscribeArticleList) -> {
            DailyArticleDto dto = new DailyArticleDto();
            dto.setIsPageEnd(true);
            dto.setDate(DateUtils.getSpecialDateFormat(date));
            dto.setArticleList(subscribeArticleList);
            dailyArticleGroup.add(dto);
        });

        BibleRefreshListDto<DailyArticleDto> result = new BibleRefreshListDto<>();
        result.setList(dailyArticleGroup);
        // 查看是否firstOpen
        result.setFirstOpen(subscribeArticleService.isFirstOpenBible(loginUser.getId()));
        result.setIsDateEnd(subscribeArticleService.isLastArticleDate(dateStr));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/load/article/certain/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadArticleGroup(LoginUser loginUser, @ModelAttribute Page page, @PathVariable String date) {
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
        List<SubscribeArticle> subscribeArticles = subscribeArticleService.loadSubscribeArticleList(loginUser.getId(), page, date);
        List<DailyArticleDto> dailyArticleGroup = Lists.newArrayList();
        DailyArticleDto dto = new DailyArticleDto();
        dailyArticleGroup.add(dto);
        dto.setArticleList(subscribeArticles);
        dto.setDate(DateUtils.getSpecialDateFormat(DateUtils.parseStringToDate7(date)));
        dto.setIsPageEnd(page.isLastPage());
        BibleRefreshListDto<DailyArticleDto> result = new BibleRefreshListDto<>();
        result.setList(dailyArticleGroup);
        result.setIsDateEnd(subscribeArticleService.isLastArticleDate(date));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/favor/article/{articleId}", method = RequestMethod.POST)
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


    @RequestMapping(value = "/disfavor/article/{articleId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> disfavorArticle(LoginUser loginUser, @PathVariable Integer articleId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("文章")
                .action("喜欢");
        operationLogService.log(operationLog);
        Boolean result = subscribeArticleService.disfavorArticle(loginUser.getId(), articleId);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/open/article/{articleId}", method = RequestMethod.POST)
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
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("分数")
                .action("获取");
        operationLogService.log(operationLog);
        List<SubscribePointCompare> compareList = subscribeArticleService.loadSubscribeViewPointList(loginUser.getId());
        BibleScore bibleScore = new BibleScore();
        Profile profile = accountService.getProfile(loginUser.getId());
        bibleScore.setRiseId(profile.getRiseId());
        bibleScore.setCompareGroup(compareList);
        bibleScore.setNickName(profile.getNickname());
        bibleScore.setHeadImage(profile.getHeadimgurl());
        bibleScore.setTotalWords(subscribeArticleService.loadCertainDayReadWords(loginUser.getId(), new Date()));
        bibleScore.setQrCode(subscribeArticleService.loadUserQrCode(loginUser.getId()));
        return WebUtils.result(bibleScore);
    }

    /**
     * 游客浏览，不校验个人信息
     */
    @RequestMapping(value = "/guest/load/score", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadScoreForGuest(@RequestParam(value = "riseId") String riseId, @RequestParam(value = "date") String dateStr) {
        Profile profileByRiseId = accountService.getProfileByRiseId(riseId);
        Date date = DateUtils.parseStringToDate7(dateStr);
        List<SubscribePointCompare> compareList = subscribeArticleService.loadSubscribeViewPointList(profileByRiseId.getId());
        BibleScore bibleScore = new BibleScore();
        Profile profile = accountService.getProfile(profileByRiseId.getId());
        bibleScore.setRiseId(profile.getRiseId());
        bibleScore.setCompareGroup(compareList);
        bibleScore.setNickName(profile.getNickname());
        bibleScore.setHeadImage(profile.getHeadimgurl());
        bibleScore.setTotalWords(subscribeArticleService.loadCertainDayReadWords(profileByRiseId.getId(), date));
        bibleScore.setQrCode(subscribeArticleService.loadUserQrCode(profileByRiseId.getId()));
        return WebUtils.result(bibleScore);
    }

    @RequestMapping(value = "/open/bible", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openBible(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("状态")
                .action("第一次打开bible");
        operationLogService.log(operationLog);
        Boolean result = subscribeArticleService.openBible(loginUser.getId());
        return WebUtils.result(result);
    }
}
