package com.iquanwai.platon.biz.domain.common.customer;


import com.iquanwai.platon.biz.po.common.Profile;

/**
 * Created by nethunder on 2017/2/8.
 */
public interface ProfileService {
    /**
     * 根据openId获取用户信息
     */
    Profile getProfile(String openId);

    /**
     * 在个人中心里提交用户信息
     */
    void submitPersonalCenterProfile(Profile profile);

    /**
     * 在报名以及毕业证书前的那个页面提交用户信息
     */
    void submitPersonalInfo(Profile profile, Boolean risePoint);
}