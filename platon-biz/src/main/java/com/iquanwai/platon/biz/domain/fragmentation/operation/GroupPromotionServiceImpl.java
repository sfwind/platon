package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.dao.fragmentation.AuditionClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.GroupPromotionDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import com.iquanwai.platon.biz.po.GroupPromotion;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.support.Assert;

import java.util.List;


@Service
public class GroupPromotionServiceImpl implements GroupPromotionService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private GroupPromotionDao groupPromotionDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 推广成功人数
    private static int GROUP_PROMOTION_SUCCESS_COUNT = 3;

    @Override
    public boolean checkGroupPromotionAuthority(String openId) {
        // 查看用户是否关注
        try {
            Account account = accountService.getAccount(openId, false);
            if (account == null) {
                return true;
            } else {
                Profile profile = accountService.getProfile(account.getOpenid());
                if (profile == null) {
                    return true;
                } else {
                    int profileId = profile.getId();
                    // 用户没有付费并且没有参加试听才有资格
                    List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
                    AuditionClassMember auditionClassMember = auditionClassMemberDao.loadByProfileId(profileId);
                    if (riseMembers.size() == 0 && auditionClassMember == null) {
                        return true;
                    }
                }
            }
        } catch (NotFollowingException e) {
            // 从未关注用户，有参加活动权限
            return true;
        }
        return false;
    }

    @Override
    public GroupPromotion createGroup(Integer profileId) {
        GroupPromotion existPromotion = groupPromotionDao.loadByProfileId(profileId);
        if (existPromotion != null) {
            return existPromotion;
        }

        String groupCode = CommonUtils.randomString(16);
        GroupPromotion groupPromotion = new GroupPromotion();
        groupPromotion.setProfileId(profileId);
        groupPromotion.setGroupCode(groupCode);
        groupPromotion.setLeader(true);
        groupPromotionDao.insert(groupPromotion);
        return groupPromotion;
    }

    @Override
    public boolean participateGroup(Integer profileId, String groupCode) {
        List<GroupPromotion> groupPromotions = groupPromotionDao.loadByGroupCode(groupCode);
        Assert.isTrue(groupPromotions.size() > 0, "团队编号不存在");

        GroupPromotion groupPromotion = new GroupPromotion();
        groupPromotion.setProfileId(profileId);
        groupPromotion.setGroupCode(groupCode);
        groupPromotion.setLeader(false);
        int insertResult = groupPromotionDao.insert(groupPromotion);
        if (insertResult > 0) {
            if (groupPromotions.size() == 1) {
                // 如果响应了一个新晋团队，则将团长开课
                generatePlanService.createTeamLearningPlan(groupPromotions.get(0).getProfileId());
                logger.info("给团长开课");
            }
            // 给自己开课
            generatePlanService.createTeamLearningPlan(profileId);
            logger.info("给自己开课");
            logger.info("发送参团成功消息");
            sendParticipateSuccessLatest(profileId, groupCode);
        }

        return insertResult > 0;
    }

    @Override
    public boolean participateGroup(String openId, String groupCode) {
        Profile profile = accountService.getProfile(openId);
        Assert.notNull(profile, "扫码用户不能为空");
        return participateGroup(profile.getId(), groupCode);
    }

    @Override
    public boolean hasParticipateGroup(Integer profileId) {
        GroupPromotion groupPromotion = groupPromotionDao.loadByProfileId(profileId);
        return groupPromotion != null;
    }

    // 新用户接收消息
    private void sendParticipateSuccessLatest(Integer newProfileId, String groupCode) {
        Profile newProfile = accountService.getProfile(newProfileId);
        List<GroupPromotion> currentGroupPromotions = groupPromotionDao.loadByGroupCode(groupCode);

        String ordinarySuccessMessage = "你已组团完毕，成功领取168元7天学习资格。1月7日晚20点课程正式开始，开课后记得评价你的好友，告诉ta了解你眼中的ta哦~";
        // 距离目标完成人数
        int remainderCount = GROUP_PROMOTION_SUCCESS_COUNT - currentGroupPromotions.size();
        if (remainderCount < 0) {
            customerMessageService.sendCustomerMessage(newProfile.getOpenid(), ordinarySuccessMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            return;
        }

        GroupPromotion leaderPromotion = currentGroupPromotions.stream().filter(GroupPromotion::getLeader).findAny().orElse(null);
        Assert.notNull(leaderPromotion, "团队创建人不能为空");
        Profile leaderProfile = accountService.getProfile(leaderPromotion.getProfileId());

        String newProfileMessage;
        if (remainderCount > 0) {
            newProfileMessage = "你已接受" + leaderProfile.getNickname() + "邀请，还差" + remainderCount + "人加入开启7天学习资格";
        } else {
            newProfileMessage = ordinarySuccessMessage;
        }
        customerMessageService.sendCustomerMessage(newProfile.getOpenid(), newProfileMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);

        String oldProfileMessage;
        if (remainderCount > 0) {
            oldProfileMessage = newProfile.getNickname() + "已接受邀请，加入学习。还差" + remainderCount + "人加入领取7天学习资格，分享邀请链接，和你的好友一起7天学习。如有疑问XXXX";
        } else {
            oldProfileMessage = "你已组团完毕，成功领取168元7天学习资格。1月7日晚20点课程正式开始，开课后记得评价你的好友，告诉ta了解你眼中的ta哦~";
        }
        customerMessageService.sendCustomerMessage(newProfile.getOpenid(), oldProfileMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

}
