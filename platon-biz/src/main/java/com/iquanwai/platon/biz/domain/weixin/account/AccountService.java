package com.iquanwai.platon.biz.domain.weixin.account;


import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.common.*;
import org.apache.commons.lang3.tuple.Pair;

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
     * 根据 profileId 获取用户角色信息
     */
    UserRole getUserRole(Integer profileId);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid);

    /**
     * 根据id获取用户详情
     */
    Profile getProfile(Integer profileId);

    /**
     * 根据openid批量获取用户详情
     */
    List<Profile> getProfiles(List<Integer> profileIds);
    /**
     * 获取所有的省份信息
     * */
    List<Region> loadAllProvinces();

    /**
     * 获取某省份的城市信息
     * */
    List<Region> loadCities();
    /**
     * 更新是否打开导航栏
     */
    int updateOpenNavigator(Integer id);
    /**
     * 更新是否打开rise
     */
    int updateOpenRise(Integer id);
    /**
     * 更新是否打开rise应用练习
     */
    int updateOpenApplication(Integer id);
    /**
     * 更新是否打开rise巩固练习
     */
    int updateOpenConsolidation(Integer id);

    /**
     * 在个人中心里提交用户信息
     */
    void submitPersonalCenterProfile(Profile profile);

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";

    void reloadRegion();

    Region loadProvinceByName(String name);

    Region loadCityByName(String name);

    Role getRole(Integer profileId);

    /**
     * 发送验证码
     * @param phone 手机号码
     * @param profileId 用户id
     * @param areaCode 区号
     */
    Pair<Boolean, String> sendValidCode(String phone, Integer profileId, String areaCode);

    /**
     * 验证验证码
     * @param profileId 用户id
     * @param code 用户输入的验证码
     */
    boolean validCode(String code, Integer profileId);

    /**
     * 是否是rise会员
     * @param profileId profileId
     */
    Boolean isRiseMember(Integer profileId);

    /**
     * 获取优惠券信息
     */
    List<Coupon> loadCoupons(Integer profileId);

    void insertCoupon(Coupon coupon);
}
