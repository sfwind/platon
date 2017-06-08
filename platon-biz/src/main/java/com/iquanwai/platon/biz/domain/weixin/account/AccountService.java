package com.iquanwai.platon.biz.domain.weixin.account;


import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.common.*;

import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {
    /**
     * 根据openid获取用户的详细信息
     * */
    Account getAccount(String openid, boolean realTime) throws NotFollowingException;

    /**
     * 根据 openid 获取用户角色信息
     */
    UserRole getUserRole(String openid);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid, boolean realTime);

    /**
     * 根据id获取用户详情
     */
    Profile getProfile(Integer profileId);

    /**
     * 根据openid批量获取用户详情
     */
    List<Profile> getProfiles(List<String> openid);
    /**
     * 获取所有的省份信息
     * */
    List<Region> loadAllProvinces();

    /**
     * 获取某省份的城市信息
     * */
    List<Region> loadCities();
    /**
     * 更新是否打开rise
     */
    int updateOpenRise(String openId);
    /**
     * 更新是否打开rise应用练习
     */
    int updateOpenApplication(String openId);
    /**
     * 更新是否打开rise巩固练习
     */
    int updateOpenConsolidation(String openId);

    /**
     * 在个人中心里提交用户信息
     */
    void submitPersonalCenterProfile(Profile profile);

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    void reloadRegion();

    Region loadProvinceByName(String name);

    Region loadCityByName(String name);
}
