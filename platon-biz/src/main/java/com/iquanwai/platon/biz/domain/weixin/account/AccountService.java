package com.iquanwai.platon.biz.domain.weixin.account;

import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.*;
import com.iquanwai.platon.biz.po.user.UserInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AccountService {

    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";
    String GUEST_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}&lang=zh_CN";
    String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";

    /**
     * new
     * 调用 confucius 接口，生成并补充新的用户信息
     */
    boolean initUserByUnionId(String unionId, Boolean realTime);

    boolean checkIsSubscribe(String openId, String unionId);

    /**
     * 根据 profileId 获取用户角色信息
     */
    UserRole getUserRole(Integer profileId);

    Profile getProfileByUnionId(String unionId);

    /**
     * 根据openid获取用户详情
     */
    Profile getProfile(String openid);

    Profile getProfileByRiseId(String riseId);

    /**
     * 根据id获取用户详情
     */
    Profile getProfile(Integer profileId);

    /**
     * 根据openid批量获取用户详情
     */
    List<Profile> getProfiles(List<Integer> profileIds);

    Account getAccountByUnionId(String unionId);

    /**
     * 获取所有的省份信息
     */
    List<Region> loadAllProvinces();

    /**
     * 获取某省份的城市信息
     */
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
     * 更新是否打开rise巩固练习
     */
    int updateOpenWelcome(Integer id);

    /**
     * 在个人中心里提交用户信息
     */
    void submitPersonalCenterProfile(Profile profile, UserInfo userInfo);


    /**
     * 查看证书时提交用户信息
     */
    void submitCertificateProfile(Profile profile);

    void reloadRegion();

    Region loadProvinceByName(String name);

    Region loadCityByName(String name);

    Role getRole(Integer profileId);

    /**
     * 获取助教角色
     */
    UserRole getAssist(Integer profileId);

    /**
     * 发送验证码
     *
     * @param phone     手机号码
     * @param profileId 用户id
     * @param areaCode  区号
     */
    Pair<Boolean, String> sendValidCode(String phone, Integer profileId, String areaCode);

    /**
     * 验证验证码
     *
     * @param profileId 用户id
     * @param code      用户输入的验证码
     */
    boolean validCode(String code, Integer profileId);

    /**
     * 获取优惠券信息
     *
     * @param profileId 用户id
     */
    List<Coupon> loadCoupons(Integer profileId);


    /**
     * 打开每日学习提醒
     *
     * @param profileId 用户id
     * @return 是否操作成功
     */
    Boolean openLearningNotify(Integer profileId);

    /**
     * 关闭每日学习提醒
     *
     * @param profileId 用户id
     * @return 是否操作成功
     */
    Boolean closeLearningNotify(Integer profileId);

    /**
     * 是否有对应status
     *
     * @param profileId 用户id
     * @param statusId  statusId
     * @return 是否有对应statusId
     */
    Boolean hasStatusId(Integer profileId, Integer statusId);


    /**
     * 创建关注推送消息
     *
     * @param openid   openId
     * @param callback 回调地址
     * @param scene    场景值
     * @return base64图片
     */
    String createSubscribePush(String openid, String callback, String scene);

    /**
     * 获取关注事件推送消息
     *
     * @param id 推送id
     * @return 事件消息
     */
    SubscribePush loadSubscribePush(Integer id);

    /**
     * 获取用户的课程表类型
     *
     * @param profileId 用户id
     * @return 课程表类型: <br/>
     * <ul>
     * <li>1-新用户</li>
     * <li>2-老用户</li>
     * </ul>
     */
    Integer loadUserScheduleCategory(Integer profileId);

    /**
     * 更新微信id
     */
    void updateWeixinId(Integer profileId, String weixinId);

    /**
     * 根据学号获取用户 id
     */
    List<Integer> getProfileIdsByMemberId(List<String> memberIds);

    @Deprecated
    RiseMember getValidRiseMember(Integer profileId);

    String getOpenidByMemberId(String memberId);
}
