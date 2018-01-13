package com.iquanwai.platon.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.web.util.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginUserService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private ProfileDao profileDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PC_TOKEN_COOKIE_NAME = "_qt";
    public static final String WE_CHAT_TOKEN_COOKIE_NAME = "_act";

    public static final String WE_MINI_STATE_HEADER_NAME = "sk";

    public static final String ACCESS_ASK_TOKEN_COOKIE_NAME = "_ask";
    private static final String PLATFORM_HEADER_NAME = "platform";

    /** 用来缓存已经登录的用户 */
    private static Map<String, SoftReference<LoginUser>> pcLoginUserMap = Maps.newHashMap(); // pc的登录缓存
    private static Map<String, SoftReference<LoginUser>> weMobileLoginUserMap = Maps.newHashMap(); // 微信的登录缓存
    private static Map<String, SoftReference<LoginUser>> weMiniLoginUserMap = Maps.newHashMap(); // 小程序的登录缓存

    private static Map<String, SoftReference<LoginUser>> loginUserCacheMap = Maps.newConcurrentMap(); // 用户登录缓存

    private static List<String> waitPCRefreshOpenids = Lists.newArrayList(); // 待更新openid
    private static List<String> waitWechatRefreshOpenids = Lists.newArrayList(); // 待更新openid

    // 待更新信息用户的 unionId 集合
    private static List<String> waitRefreshUnionIds = Lists.newArrayList();

    // /**
    //  * 登录，就是缓存起来
    //  * @param sessionId sessionId
    //  * @param loginUser 用户
    //  */
    // public void login(LoginUser.Platform platform, String sessionId, LoginUser loginUser) {
    //     switch (platform) {
    //         case PC:
    //             loginUser.setDevice(Constants.Device.PC);
    //             pcLoginUserMap.put(sessionId, new SoftReference<>(loginUser));
    //             break;
    //         case WE_MOBILE:
    //             loginUser.setDevice(Constants.Device.MOBILE);
    //             weMobileLoginUserMap.put(sessionId, new SoftReference<>(loginUser));
    //             break;
    //         default:
    //     }
    // }

    // /**
    //  * 根据sessionId判断用户是否登录
    //  * @param sessionId SessionId
    //  * @return 是否登录
    //  */
    // public boolean isLogin(LoginUser.Platform platform, String sessionId) {
    //     LoginUser loginUser = this.loadUser(platform, sessionId);
    //     if (loginUser != null) {
    //         // 只在未登录的时候打印
    //         return true;
    //     } else {
    //         // 只有没登录时会打印一次
    //         logger.info("cookie:{},没有登录,platform:{}", sessionId, platform);
    //         return false;
    //     }
    // }

    // /**
    //  * -1 key查不到callback，清除掉
    //  * -2 key查到了，但是获取不到user，应该是没点服务号
    //  * 1 成功
    //  */
    // public Pair<Integer, Callback> refreshLogin(LoginUser.Platform platform, String sessionId) {
    //     // 有key但是没有value，重新查一遍
    //     // 先检查这个cookie是否合法
    //     Callback callback = null;
    //     switch (platform) {
    //         case PC:
    //             callback = callbackDao.queryByPcAccessToken(sessionId);
    //             break;
    //         case WE_MOBILE:
    //             callback = callbackDao.queryByAccessToken(sessionId);
    //             break;
    //         default:
    //             return new MutablePair<>(-1, null);
    //     }
    //     if (callback == null) {
    //         // 不合法
    //         return new MutablePair<>(-1, null);
    //     } else {
    //         // 合法，再查一遍
    //         Pair<Integer, LoginUser> result = getLoginUser(platform, sessionId);
    //         if (result.getLeft() < 0) {
    //             logger.info("platform:{},key:{} is lost , remove cookie", platform, sessionId);
    //             switch (platform) {
    //                 case PC:
    //                     pcLoginUserMap.remove(sessionId);
    //                     break;
    //                 case WE_MOBILE:
    //                     weMobileLoginUserMap.remove(sessionId);
    //                     break;
    //                 default:
    //                     logger.error("异常的平台信息");
    //             }
    //             return new MutablePair<>(-2, callback);
    //         } else {
    //             logger.info("platform:{},key:{} is lost , search again: {}", platform, sessionId, result.getRight());
    //             login(platform, sessionId, result.getRight());
    //             return new MutablePair<>(1, callback);
    //         }
    //     }
    // }

    // /**
    //  * 获取PCLoginUser
    //  * @return -1:没有cookie <br/>
    //  * -2:accessToken无效<br/>
    //  * -3:没有关注<br/>
    //  * 1:PCLoginUser
    //  */
    // @Deprecated
    // public Pair<Integer, LoginUser> getLoginUser(HttpServletRequest request) {
    //     String pcToken = CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
    //     String wechatToken = CookieUtils.getCookie(request, LoginUserService.WE_CHAT_TOKEN_COOKIE_NAME);
    //     if (StringUtils.isEmpty(pcToken) && StringUtils.isEmpty(wechatToken)) {
    //         return new MutablePair<>(-1, null);
    //     } else {
    //         if (!StringUtils.isEmpty(pcToken)) {
    //             return getLoginUser(LoginUser.Platform.PC, pcToken);
    //         } else {
    //             return getLoginUser(LoginUser.Platform.WE_MOBILE, wechatToken);
    //         }
    //     }
    // }

    // public Boolean isLogin(HttpServletRequest request) {
    //     LoginUser.Platform platform = checkPlatform(request);
    //     String token = getToken(request);
    //     return isLogin(platform, token);
    // }

    /**
     * 刷新用户信息适配 openid 参数，主要是解决上传头像问题
     * @param openId 微信手机端 openId
     */
    // new
    public void updateLoginUserByOpenId(String openId) {
        Profile profile = profileDao.queryByOpenId(openId);
        if (profile != null) {
            waitRefreshUnionIds.add(profile.getUnionid());
        }
    }

    /**
     * 刷新用户 unionId
     * @param unionId unionId
     */
    // new
    public void updateLoginUserByUnionId(String unionId) {
        waitRefreshUnionIds.add(unionId);
    }

    public void logoutLoginUser(String pcSessionId) {
        // TODO 适配原先此处 sessionId 特指的 pcAccessToken
        Callback callback = callbackDao.queryByPcAccessToken(pcSessionId);
        if (callback != null) {
            loginUserCacheMap.remove(callback.getState());
        }
    }

    // @Deprecated
    // public String getToken(HttpServletRequest request) {
    //     LoginUser.Platform platform = getPlatformType(request);
    //     switch (platform) {
    //         case PC:
    //             return CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
    //         case WE_MOBILE:
    //             return CookieUtils.getCookie(request, LoginUserService.WE_CHAT_TOKEN_COOKIE_NAME);
    //         default:
    //             return null;
    //     }
    // }

    // @Deprecated
    // public LoginUser.Platform checkPlatform(HttpServletRequest request) {
    //     String platform = request.getHeader(PLATFORM_HEADER_NAME);
    //     if (LoginUser.PlatformHeaderValue.WE_MINI_HEADER.equals(platform)) {
    //         // 小程序
    //         return LoginUser.Platform.WE_MINI;
    //     } else {
    //         String pcToken = CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
    //         String wechatToken = CookieUtils.getCookie(request, LoginUserService.WE_CHAT_TOKEN_COOKIE_NAME);
    //         if (StringUtils.isEmpty(pcToken) && StringUtils.isEmpty(wechatToken)) {
    //             // pcToken和wechatToken都为null则是移动
    //             return LoginUser.Platform.WE_MOBILE;
    //         }
    //         if (!StringUtils.isEmpty(pcToken)) {
    //             return LoginUser.Platform.PC;
    //         }
    //         if (!StringUtils.isEmpty(wechatToken)) {
    //             return LoginUser.Platform.WE_MOBILE;
    //         }
    //         // 两个都存在就默认为移动
    //         return LoginUser.Platform.WE_MOBILE;
    //     }
    // }

    // public String openId(LoginUser.Platform platform, String accessToken) {
    //     switch (platform) {
    //         case PC:
    //             return oAuthService.pcOpenId(accessToken);
    //         case WE_MOBILE:
    //             return oAuthService.openId(accessToken);
    //         default:
    //             return null;
    //     }
    // }

    // new
    public LoginUser getLoginUserByRequest(HttpServletRequest request) {
        LoginUser.Platform platform = getPlatformType(request);
        Callback callback;
        switch (platform) {
            case PC:
                String pcAccessToken = CookieUtils.getCookie(request, PC_TOKEN_COOKIE_NAME);
                if (StringUtils.isEmpty(pcAccessToken)) return null;
                callback = callbackDao.queryByPcAccessToken(pcAccessToken);
                break;
            case WE_MOBILE:
                String weMobileAccessToken = CookieUtils.getCookie(request, WE_CHAT_TOKEN_COOKIE_NAME);
                if (StringUtils.isEmpty(weMobileAccessToken)) return null;
                callback = callbackDao.queryByAccessToken(weMobileAccessToken);
                break;
            case WE_MINI:
                String weMiniState = request.getHeader(WE_MINI_STATE_HEADER_NAME);
                if (StringUtils.isEmpty(weMiniState)) return null;
                callback = callbackDao.queryByState(weMiniState);
                break;
            default:
                callback = null;
        }
        LoginUser loginUser = getLoginUserByCallback(callback);
        // 填充设备信息
        loginUser.setDevice(platform.getValue());
        return loginUser;
    }

    /**
     * 根据请求获取当前请求所在平台
     * @param request 请求
     * @return 平台类型
     */
    // new
    public LoginUser.Platform getPlatformType(HttpServletRequest request) {
        String platformHeader = request.getHeader(PLATFORM_HEADER_NAME);
        Assert.notNull(platformHeader);
        switch (platformHeader) {
            case LoginUser.PlatformHeaderValue.PC_HEADER:
                return LoginUser.Platform.PC;
            case LoginUser.PlatformHeaderValue.WE_MOBILE_HEADER:
                return LoginUser.Platform.WE_MOBILE;
            case LoginUser.PlatformHeaderValue.WE_MINI_HEADER:
                return LoginUser.Platform.WE_MINI;
            default:
                return LoginUser.Platform.PC;
        }
    }

    /**
     * 根据回调对象返回登录对象
     * @param callback 回调
     */
    // new
    private LoginUser getLoginUserByCallback(Callback callback) {
        String state = callback.getState();
        String unionId = callback.getUnionId();
        LoginUser loginUser;

        if (waitRefreshUnionIds.contains(unionId)) {
            waitRefreshUnionIds.remove(unionId);
            logger.info("刷新 UnionId 为 {} 用户缓存", unionId);
            Profile profile = profileDao.queryByUnionId(unionId);
            loginUser = buildLoginUserDetail(profile);
            // 将最新数据放进缓存
            loginUserCacheMap.put(state, new SoftReference<>(loginUser));
        } else {
            if (loginUserCacheMap.containsKey(state)) {
                loginUser = loginUserCacheMap.get(state).get();
                if (loginUser == null) {
                    // 软连接，存储对象被回收
                    Profile profile = profileDao.queryByUnionId(unionId);
                    loginUser = buildLoginUserDetail(profile);
                    loginUserCacheMap.put(state, new SoftReference<>(loginUser));
                }
            } else {
                // 缓存中不存在，直接从数据库中读取数据
                Profile profile = profileDao.queryByUnionId(unionId);
                loginUser = buildLoginUserDetail(profile);
                loginUserCacheMap.put(state, new SoftReference<>(loginUser));
            }
        }
        return loginUser;
    }

    // /**
    //  * 获取PCLoginUser
    //  * -1:没有cookie <br/>
    //  * -2:accessToken无效,没有点页面<br/>
    //  * -3:没有关注，一般不会走到这个<br/>
    //  * -4:一般是没有关注服务号
    //  * 1:PCLoginUser
    //  */
    // @Deprecated
    // public Pair<Integer, LoginUser> getLoginUser(LoginUser.Platform platform, String accessToken) {
    //     // 先检查有没有缓存
    //     LoginUser loginUser = this.loadUser(platform, accessToken);
    //     if (loginUser != null) {
    //         return new MutablePair<>(1, loginUser);
    //     }
    //
    //     String openid = this.openId(platform, accessToken);
    //     if (openid == null) {
    //         // 没有查到openid，一般是该用户没有关注服务号
    //         logger.info("accessToken:{} can't find openId", accessToken);
    //         return new MutablePair<>(-4, null);
    //     }
    //     Account account;
    //     try {
    //         account = accountService.getAccount(openid, false);
    //     } catch (NotFollowingException e) {
    //         return new MutablePair<>(-3, null);
    //     }
    //     logger.info("platform:{},accessToken:{},openId:{},account:{}", platform, accessToken, openid, account);
    //     if (account == null) {
    //         return new MutablePair<>(-2, null);
    //     }
    //
    //     // 重新加载loginUser
    //     loginUser = getLoginUser(openid, platform);
    //     logger.info("user:{}", loginUser);
    //     return new MutablePair<>(1, loginUser);
    // }

    // @Deprecated
    // private LoginUser loadUser(LoginUser.Platform platform, String accessToken) {
    //     LoginUser loginUser = null;
    //     switch (platform) {
    //         case PC:
    //             if (pcLoginUserMap.get(accessToken) != null) {
    //                 loginUser = pcLoginUserMap.get(accessToken).get();
    //             }
    //             // 如果数据待更新,则读取数据库
    //             if (loginUser != null) {
    //                 String openid = loginUser.getOpenId();
    //                 if (waitPCRefreshOpenids.contains(openid)) {
    //                     logger.info("更新用户{}", openid);
    //                     loginUser = getLoginUser(openid, platform);
    //                     pcLoginUserMap.put(accessToken, new SoftReference<>(loginUser));
    //                     waitPCRefreshOpenids.remove(openid);
    //                 }
    //             }
    //             return loginUser;
    //         case WE_MOBILE:
    //             if (weMobileLoginUserMap.get(accessToken) != null) {
    //                 loginUser = weMobileLoginUserMap.get(accessToken).get();
    //             }
    //             // 如果数据待更新,则读取数据库
    //             if (loginUser != null) {
    //                 String openid2 = loginUser.getOpenId();
    //                 if (waitWechatRefreshOpenids.contains(openid2)) {
    //                     logger.info("更新用户{}", openid2);
    //                     loginUser = getLoginUser(openid2, platform);
    //                     weMobileLoginUserMap.put(accessToken, new SoftReference<>(loginUser));
    //                     waitWechatRefreshOpenids.remove(openid2);
    //                 }
    //             }
    //             return loginUser;
    //         default:
    //             return null;
    //     }
    // }

    /**
     * 对用户信息进行过滤筛选
     */
    // new
    private LoginUser buildLoginUserDetail(Profile profile) {
        Role role = this.getUserRole(profile.getId());
        LoginUser loginUser = new LoginUser();
        loginUser.setId(profile.getId());
        loginUser.setOpenId(profile.getOpenid());
        loginUser.setWeixinName(profile.getNickname());
        loginUser.setHeadimgUrl(profile.getHeadimgurl());
        loginUser.setRealName(profile.getRealName());
        loginUser.setRole(role.getId());
        loginUser.setSignature(profile.getSignature());
        loginUser.setOpenRise(profile.getOpenRise());
        loginUser.setOpenConsolidation(profile.getOpenConsolidation());
        loginUser.setOpenApplication(profile.getOpenApplication());
        loginUser.setOpenNavigator(profile.getOpenNavigator());
        loginUser.setOpenWelcome(profile.getOpenWelcome());
        loginUser.setRiseMember(profile.getRiseMember());
        return loginUser;
    }

    /** 获取当前用户的角色身份 */
    // new
    private Role getUserRole(Integer profileId) {
        Role role = accountService.getRole(profileId);
        if (role == null) {
            // 获得用户的 openid，根据 openid 查询用户的学号
            List<ImprovementPlan> plans = improvementPlanDao.loadAllPlans(profileId);
            if (plans.isEmpty()) {
                role = Role.stranger();
            } else {
                role = Role.student();
            }
        }
        return role;
    }

    // @Deprecated
    // public LoginUser getLoginUser(String openId, LoginUser.Platform platform) {
    //     Profile profile = null;
    //     try {
    //         Account temp = accountService.getAccount(openId, false);
    //         if (temp != null) {
    //             profile = accountService.getProfile(openId);
    //         }
    //     } catch (NotFollowingException e) {
    //         logger.error("异常，openId:{}，没有查到", openId);
    //         LoginUser loginUser = new LoginUser();
    //         loginUser.setOpenId(openId);
    //         return loginUser;
    //     }
    //
    //     if (profile == null) {
    //         logger.error("openId {} is not found in db", openId);
    //         return null;
    //     }
    //
    //     Role role = this.getUserRole(profile.getId());
    //     LoginUser loginUser = new LoginUser();
    //     loginUser.setId(profile.getId());
    //     loginUser.setOpenId(profile.getOpenid());
    //     loginUser.setWeixinName(profile.getNickname());
    //     loginUser.setHeadimgUrl(profile.getHeadimgurl());
    //     loginUser.setRealName(profile.getRealName());
    //     loginUser.setRole(role.getId());
    //     loginUser.setSignature(profile.getSignature());
    //     loginUser.setOpenRise(profile.getOpenRise());
    //     loginUser.setOpenConsolidation(profile.getOpenConsolidation());
    //     loginUser.setOpenApplication(profile.getOpenApplication());
    //     loginUser.setOpenNavigator(profile.getOpenNavigator());
    //     loginUser.setOpenWelcome(profile.getOpenWelcome());
    //     loginUser.setRiseMember(profile.getRiseMember());
    //     return loginUser;
    // }

    // new
    public static List<LoginUser> getAllUsers() {
        List<LoginUser> list = Lists.newArrayList();
        // list.addAll(pcLoginUserMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        // list.addAll(weMobileLoginUserMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        list.addAll(loginUserCacheMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        return list;
    }
}
