package com.iquanwai.platon.web.daily;

import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.daily.DailyService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private RiseMemberManager riseMemberManager;
    @Autowired
    private RedisUtil redisUtil;

    private static final Integer MINHOUR = 6;
    private static final String DAILYTALK = "daily_talk";


    @RequestMapping(value = "/talk/check", method = RequestMethod.GET)
    @ApiOperation("是否显示每日圈语")
    public ResponseEntity<Map<String, Object>> checkDailyTalk(UnionUser unionUser) {
        Integer profileId = unionUser.getId();
        Calendar c = Calendar.getInstance();
        Integer hour = c.get(Calendar.HOUR_OF_DAY);
        //6点过后才会展示
        if (hour < MINHOUR) {
            return WebUtils.result(false);
        }
        String key = DAILYTALK + "_" + profileId;
        String value = redisUtil.get(key);
        String currentDate = DateUtils.parseDateToString(new Date());
        if (value != null && value.equals(currentDate)) {
            return WebUtils.result(false);
        }

        List<RiseMember> riseMembers = riseMemberManager.member(profileId);
        //非会员不展示
        if(CollectionUtils.isEmpty(riseMembers)){
            return WebUtils.result(false);
        }else{
            return WebUtils.result(true);
        }

    }


    @RequestMapping(value = "/talk", method = RequestMethod.GET)
    @ApiOperation("获得每日圈语")
    public ResponseEntity<Map<String, Object>> getDailyTalk(UnionUser unionUser) {
        Integer profileId = unionUser.getId();
        Calendar c = Calendar.getInstance();
        Integer hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour >= MINHOUR) {
            String key = DAILYTALK + "_" + profileId;
            String value = redisUtil.get(key);
            String currentDate = DateUtils.parseDateToString(new Date());
            if (value != null && value.equals(currentDate)) {
                return WebUtils.error("当天已经显示过");
            }
            redisUtil.set(key, currentDate);

            Integer loginDay = customerService.loadContinuousLoginCount(profileId);
            Integer learnedKnowledge = customerService.loadLearnedKnowledgesCount(profileId);
            // TODO: 待验证
            List<RiseMember> riseMembers = riseMemberManager.member(profileId);
            if(CollectionUtils.isEmpty(riseMembers)){
                return WebUtils.error("非会员类型");
            }

            RiseMember riseMember = riseMembers.get(0);
            Integer percent = customerService.calSyncDefeatPercent(riseMember);

            String result = dailyService.drawDailyTalk(unionUser.getId(), currentDate, loginDay, learnedKnowledge, percent);

            if (result == null) {
                return WebUtils.error("不需要展示圈语");
            }
            return WebUtils.result(result);
        } else {
            return WebUtils.error("还未到生成时间");
        }
    }
}
