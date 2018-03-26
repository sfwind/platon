package com.iquanwai.platon.web.daily;

import com.iquanwai.platon.biz.domain.daily.DailyService;
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


    @RequestMapping(value = "/talk",method = RequestMethod.GET)
    @ApiOperation("获得每日圈语")
    public ResponseEntity<Map<String,Object>> getDailyTalk(UnionUser unionUser){
        String currentDate = DateUtils.parseDateToString(new Date());

        return WebUtils.result(dailyService.drawDailyTalk(currentDate));
    }






}
