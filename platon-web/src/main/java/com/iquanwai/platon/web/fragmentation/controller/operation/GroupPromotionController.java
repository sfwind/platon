package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.GroupPromotionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.GroupPromotion;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    @Autowired
    private QRCodeService qrCodeService;

    private DateTime groupPromotionOpenDateTime;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        groupPromotionOpenDateTime = new DateTime(2018, 1, 7, 0, 0);
    }

    @RequestMapping(value = "/participate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> participateGroup(@RequestParam("groupCode") String groupCode, LoginUser loginUser) {
        boolean hasParticipateGroup = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (hasParticipateGroup) {
            return WebUtils.success();
        }

        // 能点击参团必然是已经成功关注的人员
        boolean checkGroupPromotionAuthority = groupPromotionService.checkGroupPromotionAuthority(loginUser.getOpenId());
        if (checkGroupPromotionAuthority) {
            boolean participateResult = groupPromotionService.participateGroup(loginUser.getId(), groupCode);
            if (participateResult) {
                return WebUtils.success();
            } else {
                return WebUtils.error("用户参团失败，请练习管理员");
            }
        } else {
            return WebUtils.error("用户无参加活动权限");
        }
    }

    @RequestMapping(value = "/following")
    public ResponseEntity<Map<String, Object>> loadGroupPromotionFollowing(@RequestParam("groupCode") String groupCode, LoginUser loginUser) {
        // 页面可能无 ProfileId
        try {
            accountService.getAccount(loginUser.getOpenId(), false);
            return WebUtils.success();
        } catch (NotFollowingException e) {
            String qrCodeBase64 = qrCodeService.loadQrBase64("groupPromotion_" + groupCode);
            return WebUtils.error(201, qrCodeBase64);
        }
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGroupPromotion(LoginUser loginUser) {
        GroupPromotion groupPromotion = groupPromotionService.createGroup(loginUser.getId());
        if (groupPromotion != null) {
            return WebUtils.result(groupPromotion.getGroupCode());
        } else {
            return WebUtils.error("创建团队失败");
        }
    }

    @RequestMapping(value = "/count/down")
    public ResponseEntity<Map<String, Object>> loadGroupPromotionCountDown(LoginUser loginUser) {
        boolean hasParticipate = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (hasParticipate) {
            // 已经参加了团队学习，返回相差时间
            return WebUtils.result(String.format("%02d", DateUtils.interval(groupPromotionOpenDateTime.toDate())));
        } else {
            return WebUtils.error("无当前页面访问权限");
        }
    }

}