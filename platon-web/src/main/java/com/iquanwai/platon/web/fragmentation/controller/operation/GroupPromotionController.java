package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.GroupPromotionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.GroupPromotion;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.fragmentation.dto.GroupPromotionCountDownDto;
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

import java.util.List;
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

    // 推广成功人数
    private static final int GROUP_PROMOTION_SUCCESS_COUNT = 3;
    private static final DateTime GROUP_PROMOTION_OPEN_DATE_TIME = new DateTime(2018, 1, 7, 0, 0);

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/participate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> participateGroup(@RequestParam("groupCode") String groupCode, LoginUser loginUser) {
        GroupPromotion groupPromotion = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (groupPromotion != null) {
            return WebUtils.error("你已成功加入认识自己互助学习！");
        }

        // 能点击参团必然是已经成功关注的人员
        boolean checkGroupPromotionAuthority = groupPromotionService.checkGroupPromotionAuthority(loginUser.getOpenId());
        if (checkGroupPromotionAuthority) {
            boolean participateResult = groupPromotionService.participateGroup(loginUser.getId(), groupCode);
            if (participateResult) {
                return WebUtils.success();
            } else {
                return WebUtils.error("用户参团失败，请联系管理员");
            }
        } else {
            return WebUtils.error("您已学习过商学院课程,无需重复体验");
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
        GroupPromotion existGroupPromotion = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (existGroupPromotion != null) {
            return WebUtils.result(existGroupPromotion.getGroupCode());
        }

        boolean checkGroupPromotionAuthority = groupPromotionService.checkGroupPromotionAuthority(loginUser.getOpenId());
        if (checkGroupPromotionAuthority) {
            GroupPromotion groupPromotion = groupPromotionService.createGroup(loginUser.getId());
            if (groupPromotion != null) {
                return WebUtils.result(groupPromotion.getGroupCode());
            } else {
                return WebUtils.error("创建团队失败");
            }
        } else {
            return WebUtils.error("您已学习过商学院课程,无需重复体验");
        }
    }

    @RequestMapping(value = "/count/down")
    public ResponseEntity<Map<String, Object>> loadGroupPromotionCountDown(LoginUser loginUser) {
        GroupPromotion existGroupPromotion = groupPromotionService.hasParticipateGroup(loginUser.getId());
        if (existGroupPromotion != null) {
            List<GroupPromotion> groupPromotions = groupPromotionService.loadGroupPromotions(loginUser.getId());
            GroupPromotionCountDownDto countDownDto = new GroupPromotionCountDownDto();
            countDownDto.setIsGroupSuccess(groupPromotions.size() >= GROUP_PROMOTION_SUCCESS_COUNT);
            countDownDto.setIsLeader(groupPromotionService.isGroupLeader(loginUser.getId()));
            countDownDto.setLeaderName(groupPromotionService.loadLeaderName(loginUser.getId()));
            countDownDto.setRemainderCount(GROUP_PROMOTION_SUCCESS_COUNT - groupPromotions.size());
            // 已经参加了团队学习，返回相差时间
            countDownDto.setCountDownDay(String.format("%02d", DateUtils.interval(GROUP_PROMOTION_OPEN_DATE_TIME.toDate())));

            GroupPromotion groupPromotion = groupPromotions.stream().findAny().orElse(null);
            if (groupPromotion != null) {
                countDownDto.setGroupCode(groupPromotion.getGroupCode());
            }
            return WebUtils.result(countDownDto);
        } else {
            return WebUtils.error("无当前页面访问权限");
        }
    }

    @RequestMapping(value = "/leader")
    public ResponseEntity<Map<String, Object>> loadGroupLeader(@RequestParam("groupCode") String groupCode, LoginUser loginUser) {
        GroupPromotion groupPromotion = groupPromotionService.loadGroupLeader(groupCode);
        if (groupPromotion != null) {
            int leaderProfileId = groupPromotion.getProfileId();
            Profile profile = accountService.getProfile(leaderProfileId);
            if (profile != null) {
                Profile resultProfile = new Profile();
                resultProfile.setNickname(profile.getNickname());
                resultProfile.setHeadimgurl(profile.getHeadimgurl());
                return WebUtils.result(resultProfile);
            }
        }
        return WebUtils.error("团队创建人不存在");
    }

}