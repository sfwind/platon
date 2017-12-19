package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.GroupPromotionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

@RestController
@RequestMapping("/rise/operation/group")
public class GroupPromotionController {

    @Autowired
    private GroupPromotionService groupPromotionService;
    @Autowired
    private AccountService accountService;

    private DateTime groupPromotionOpenDateTime;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        groupPromotionOpenDateTime = new DateTime(2018, 1, 7, 0, 0);
    }

    @RequestMapping(value = "/count/down", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadGroupPromotionCountDown(LoginUser loginUser) {
        boolean hasParticipate = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (hasParticipate) {
            // 已经参加了团队学习，返回相差时间
            return WebUtils.result(String.format("%02d", DateUtils.interval(groupPromotionOpenDateTime.toDate())));
        } else {
            return WebUtils.error("无当前页面访问权限");
        }
    }

    @RequestMapping(value = "/participate/authority")
    public ResponseEntity<Map<String, Object>> loadGroupPromotionAuthority(LoginUser loginUser) {
        // 页面可能无 ProfileId
        boolean checkGroupPromotionAuthority = groupPromotionService.checkGroupPromotionAuthority(loginUser.getOpenId());
        if (checkGroupPromotionAuthority) {
            return WebUtils.success();
        } else {
            return WebUtils.error("用户无参加活动权限");
        }
    }

    @RequestMapping(value = "/following")
    public ResponseEntity<Map<String, Object>> loadGroupPromotionFollowing(LoginUser loginUser) {
        // 页面可能无 ProfileId
        try {
            accountService.getAccount(loginUser.getOpenId(), false);
            return WebUtils.success();
        } catch (NotFollowingException e) {
            return WebUtils.error("用户还未关注");
        }
    }

}
