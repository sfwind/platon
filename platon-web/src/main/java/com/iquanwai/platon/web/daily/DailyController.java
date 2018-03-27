package com.iquanwai.platon.web.daily;

import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.daily.DailyService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/rise/daily")
@Api(description = "日常活动Api")
public class DailyController {

    @Autowired
    private DailyService dailyService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/talk", method = RequestMethod.GET)
    @ApiOperation("获得每日圈语")
    public ResponseEntity<Map<String, Object>> getDailyTalk(UnionUser unionUser) {
        String currentDate = DateUtils.parseDateToString(new Date());
        Integer profileId = unionUser.getId();
//        Integer loginDay = customerService.loadContinuousLoginCount(profileId);
//        Integer learnedKnowledge = customerService.loadLearnedKnowledgesCount(profileId);
//        RiseMember riseMember = accountService.getValidRiseMember(profileId);
//        Integer percent = customerService.calSyncDefeatPercent(riseMember);

        Integer loginDay = 12;
        Integer learnedKnowledge = 43;
        Integer percent = 20;

        return WebUtils.result(dailyService.drawDailyTalk(unionUser.getId(), currentDate, loginDay, learnedKnowledge, percent));
    }
}
