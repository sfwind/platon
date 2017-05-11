package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Created by nethunder on 2017/2/8.
 */
@Service
public class ProfileServiceImpl implements ProfileService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private FollowUserDao followUserDao;
    @Autowired
    private PointRepo pointRepo;

    @Override
    public Profile getProfile(String openId) {
        return profileDao.queryByOpenId(openId);
    }

    @Override
    public void submitPersonalCenterProfile(Profile profile) {
        Assert.notNull(profile.getOpenid(), "openID不能为空");
        Profile oldProfile = profileDao.queryByOpenId(profile.getOpenid());
        Boolean result = profileDao.submitPersonalCenterProfile(profile);
        if(result && oldProfile.getIsFull()==0){
            logger.info("用户:{} 完成个人信息填写,加{}积分",profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 第一次提交，加分
            pointRepo.riseCustomerPoint(profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 更新信息状态
            profileDao.completeProfile(profile.getOpenid());
        }
    }

    @Override
    public void submitPersonalInfo(Profile profile,Boolean risePoint) {
        Assert.notNull(profile.getOpenid(), "openID不能为空");
        Profile oldProfile = profileDao.queryByOpenId(profile.getOpenid());
        Boolean result = profileDao.submitPersonalProfile(profile);
        if(result && oldProfile.getIsFull()==0 && risePoint){
            logger.info("用户:{} 完成个人信息填写,加{}积分",profile.getOpenid(),ConfigUtils.getProfileFullScore());
            // 第一次提交，加分
            pointRepo.riseCustomerPoint(profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 更新信息状态
            profileDao.completeProfile(profile.getOpenid());
        }
    }

}
