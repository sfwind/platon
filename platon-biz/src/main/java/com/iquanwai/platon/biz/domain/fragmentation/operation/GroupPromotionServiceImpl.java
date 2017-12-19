package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.dao.fragmentation.AuditionClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.GroupPromotionDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import com.iquanwai.platon.biz.po.GroupPromotion;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
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
    private GroupPromotionDao groupPromotionDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean checkGroupPromotionAuthority(String openId) {
        // 查看用户是否关注
        try {
            Account account = accountService.getAccount(openId, true);
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
    public int createGroup(Integer profileId) {
        String groupCode = CommonUtils.randomString(16);
        GroupPromotion groupPromotion = new GroupPromotion();
        groupPromotion.setProfileId(profileId);
        groupPromotion.setGroupCode(groupCode);
        groupPromotion.setLeader(true);
        return groupPromotionDao.insert(groupPromotion);
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
                // TODO 添加团长开课逻辑
                logger.info("给团长开课");
            }
            // 给自己开课
            // TODO 自己开课逻辑
            logger.info("给自己开课");
        }
        return insertResult > 0;
    }

    @Override
    public boolean participateGroup(String openId, String groupCode) {
        Profile profile = accountService.getProfile(openId);
        Assert.notNull(profile, "扫码用户不能为空");
        return participateGroup(profile.getId(), groupCode);
    }

}
