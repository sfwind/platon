package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.AuditionClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.GroupPromotionDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import com.iquanwai.platon.biz.po.GroupPromotion;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class GroupPromotionServiceImpl implements GroupPromotionService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private GroupPromotionDao groupPromotionDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;
    @Autowired
    private ProfileDao profileDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 推广成功人数
    private static final int GROUP_PROMOTION_SUCCESS_COUNT = 3;

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

        logger.info("即将入团人员Id: {}", profileId);

        // 如果已经入团，直接返回 true
        Long existPromotion = groupPromotions.stream().filter(groupPromotion -> groupPromotion.getProfileId().equals(profileId)).count();
        if (existPromotion.intValue() > 0) {
            return true;
        }

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
            sendParticipateSuccessMessage(profileId, groupCode);
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
    public GroupPromotion hasParticipateGroup(Integer profileId) {
        return groupPromotionDao.loadByProfileId(profileId);
    }

    @Override
    public List<GroupPromotion> loadGroupPromotions(Integer profileId) {
        GroupPromotion groupPromotion = groupPromotionDao.loadByProfileId(profileId);
        String groupCode = groupPromotion.getGroupCode();
        return groupPromotionDao.loadByGroupCode(groupCode);
    }

    @Override
    public GroupPromotion loadByOpenId(String openId) {
        Profile profile = accountService.getProfile(openId);
        if (profile != null) {
            return groupPromotionDao.loadByProfileId(profile.getId());
        }
        return null;
    }

    @Override
    public boolean isGroupLeader(Integer profileId) {
        GroupPromotion groupPromotion = groupPromotionDao.loadByProfileId(profileId);
        return groupPromotion.getLeader();
    }

    @Override
    public String loadLeaderName(Integer profileId) {
        GroupPromotion groupPromotion = groupPromotionDao.loadByProfileId(profileId);
        Assert.notNull(groupPromotion);

        if (groupPromotion.getLeader()) {
            Integer leaderProfileId = groupPromotion.getProfileId();
            Profile leader = accountService.getProfile(leaderProfileId);
            return leader != null ? leader.getNickname() : null;
        } else {
            String groupCode = groupPromotion.getGroupCode();
            List<GroupPromotion> groupPromotions = groupPromotionDao.loadByGroupCode(groupCode);
            GroupPromotion leaderGroupPromotion = groupPromotions.stream().filter(promotion -> promotion.getLeader()).findAny().orElse(null);
            Assert.notNull(leaderGroupPromotion);
            Integer leaderProfileId = leaderGroupPromotion.getProfileId();
            Profile leader = accountService.getProfile(leaderProfileId);
            return leader != null ? leader.getNickname() : null;
        }
    }

    // 新用户接收消息
    private void sendParticipateSuccessMessage(Integer newProfileId, String groupCode) {
        Profile newProfile = accountService.getProfile(newProfileId);
        List<GroupPromotion> currentGroupPromotions = groupPromotionDao.loadByGroupCode(groupCode);

        String ordinarySuccessMessage = "你已加入实验，解锁前7天自我认知学习和游戏内容。 \n\n1月7日晚20点正式开始，添加AI助手，回复“实验”，探寻另一个你~";
        // 距离目标完成人数
        int remainderCount = GROUP_PROMOTION_SUCCESS_COUNT - currentGroupPromotions.size();
        GroupPromotion leaderPromotion = currentGroupPromotions.stream().filter(GroupPromotion::getLeader).findAny().orElse(null);
        Assert.notNull(leaderPromotion, "团队创建人不能为空");
        Profile leaderProfile = accountService.getProfile(leaderPromotion.getProfileId());

        // 给新人发送消息
        if (remainderCount > 0) {
            String newProfileMessage = "你已接受" + leaderProfile.getNickname() + "邀请，还差" + remainderCount + "人加入解锁7天实验，请等待解锁成功通知。你可以<a href='"
                    + ConfigUtils.domainName() + "/pay/static/camp/group?groupCode=" + groupCode + "&share=true" + "'>邀请更多好友加入</a>。";
            customerMessageService.sendCustomerMessage(newProfile.getOpenid(), newProfileMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            customerMessageService.sendCustomerMessage(newProfile.getOpenid(), ordinarySuccessMessage, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            customerMessageService.sendCustomerMessage(newProfile.getOpenid(), ConfigUtils.getTeamPromotionCodeImage(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        }

        // 给不是自己的老人发送消息
        if (remainderCount > 0) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(leaderProfile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
            templateMessage.setUrl(ConfigUtils.domainName() + "/pay/static/camp/group?groupCode=" + groupCode + "&share=true");
            data.put("first", new TemplateMessage.Keyword(newProfile.getNickname() + "已接受邀请。还差最后" + remainderCount + "人加入，" + GROUP_PROMOTION_SUCCESS_COUNT + "人免费解锁前7天实验。\n"));
            data.put("keyword1", new TemplateMessage.Keyword("自我认知实验"));
            data.put("keyword2", new TemplateMessage.Keyword("截止1月7日晚20:00"));
            data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
            data.put("remark", new TemplateMessage.Keyword("\n点击详情分享邀请链接，邀请更多好友。如有疑问请在下方留言。"));
            templateMessageService.sendMessage(templateMessage);
        } else if (remainderCount == 0) {
            List<GroupPromotion> oldPromotionUsers = currentGroupPromotions.stream()
                    .filter(groupPromotion -> !groupPromotion.getProfileId().equals(newProfileId)).collect(Collectors.toList());
            List<Integer> oldPromotionUsersProfileIds = oldPromotionUsers.stream().map(GroupPromotion::getProfileId).collect(Collectors.toList());
            List<Profile> profiles = profileDao.queryAccounts(oldPromotionUsersProfileIds);
            TemplateMessage templateMessage = new TemplateMessage();

            profiles.forEach(profile -> {
                templateMessage.setTouser(profile.getOpenid());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setTemplate_id(ConfigUtils.getApplySuccessNotice());
                templateMessage.setUrl(ConfigUtils.getTeamPromotionCodeUrl());
                data.put("first", new TemplateMessage.Keyword("你已加入实验，成功解锁前7天自我认知学习和游戏内容。\n"));
                data.put("keyword1", new TemplateMessage.Keyword("认识自己|用冰山模型，分析出真实的你"));
                data.put("keyword2", new TemplateMessage.Keyword("2017.01.07 - 2017.01.14"));
                data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
                data.put("remark", new TemplateMessage.Keyword("\n点击详情，添加AI助手探寻另一个你~"));
                templateMessageService.sendMessage(templateMessage);
                customerMessageService.sendCustomerMessage(profile.getOpenid(), ConfigUtils.getTeamPromotionCodeImage(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
            });
        }
    }

}
