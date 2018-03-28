package com.iquanwai.platon.web.daily;

import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.daily.DailyService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.daily.DailyTalk;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
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
    @Autowired
    private RedisUtil redisUtil;

    private static final Integer minHour = 20;


    @RequestMapping(value = "/talk", method = RequestMethod.GET)
    @ApiOperation("获得每日圈语")
    public ResponseEntity<Map<String, Object>> getDailyTalk(UnionUser unionUser) {

        //TODO:当前时间点是否大于6点
        Calendar c = Calendar.getInstance();
        Integer hour = c.get(Calendar.HOUR_OF_DAY);
        if(hour>=minHour){
            String currentDate = DateUtils.parseDateToString(new Date());
            Integer profileId = unionUser.getId();
            //TODO:check是否是登录天数
            Integer loginDay = customerService.loadContinuousLoginCount(profileId);
            Integer learnedKnowledge = customerService.loadLearnedKnowledgesCount(profileId);
            RiseMember riseMember = accountService.getValidRiseMember(profileId);
            Integer percent = customerService.calSyncDefeatPercent(riseMember);

            return WebUtils.result(dailyService.drawDailyTalk(unionUser.getId(), currentDate, loginDay, learnedKnowledge, percent));
        }
        else {
            return WebUtils.error("还未到生成时间");
        }
        //TODO:check当天是否已经打开过
    }


//    @RequestMapping(value = "/talk/info",method = RequestMethod.GET)
//    @ApiOperation("获得每日圈语的信息")
//    public ResponseEntity<Map<String,Object>> getDailyTalkInfo(UnionUser unionUser){
//        Integer profileId = unionUser.getId();
//        DailyTalkDto dailyTalkDto = new DailyTalkDto();
//        dailyTalkDto.setLearnedDay(customerService.loadContinuousLoginCount(profileId));
//        dailyTalkDto.setLearnedKnowledge(customerService.loadLearnedKnowledgesCount(profileId));
//        RiseMember riseMember = accountService.getValidRiseMember(profileId);
//        dailyTalkDto.setPercent(customerService.calSyncDefeatPercent(riseMember));
//        dailyTalkDto.setNickName(unionUser.getNickName());
//        dailyTalkDto.setHeadImg(unionUser.getHeadImgUrl());
//
//        DailyTalk dailyTalk = dailyService.loadByTalkDate(DateUtils.parseDateToString(new Date()));
//
//        if(dailyTalk!=null){
//            BeanUtils.copyProperties(dailyTalk,dailyTalkDto);
//        }
//        dailyTalkDto.setTitle(ConfigUtils.getDailyTalkWelcome());
//
//        return WebUtils.result(dailyTalkDto);
//    }



}
