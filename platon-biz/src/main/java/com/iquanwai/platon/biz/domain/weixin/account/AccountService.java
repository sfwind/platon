package com.iquanwai.platon.biz.domain.weixin.account;


import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.po.common.Profile;

import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
public interface AccountService {
    /**
     * 根据openid获取用户的详细信息
     * */
    Account getAccount(String openid, boolean realTime);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid, boolean realTime);

    /**
     * 根据openid批量获取用户详情
     */
    List<Profile> getProfiles(List<String> openid);

    /**
     * 更新个人信息
     * */
    void submitPersonalInfo(Account account);

    /**
     * 收集所有关注用户的信息
     * */
    void collectUsers();

    /**
     * 收集新关注用户的信息
     * */
    void collectNewUsers();

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

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";


}
