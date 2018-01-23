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

    public static final String PC_STATE_COOKIE_NAME = "_qt";
    public static final String WE_CHAT_STATE_COOKIE_NAME = "_act";
    public static final String WE_MINI_STATE_HEADER_NAME = "sk";
    public static final String ACCESS_ASK_TOKEN_COOKIE_NAME = "_ask";

    private static final String PLATFORM_HEADER_NAME = "platform";

    /** 用来缓存已经登录的用户 */
    private static Map<String, SoftReference<LoginUser>> loginUserCacheMap = Maps.newConcurrentMap(); // 用户登录缓存

    /** 待更新信息用户的 unionId 集合 */
    private static List<String> waitRefreshUnionIds = Lists.newArrayList();

    /**
     * 刷新用户信息适配 openid 参数，主要是解决上传头像问题
     * @param openId 微信手机端 openId
     */
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
    public void updateLoginUserByUnionId(String unionId) {
        waitRefreshUnionIds.add(unionId);
    }

    /** 登出系统 */
    public void logoutLoginUser(String state) {
        Callback callback = callbackDao.queryByState(state);
        if (callback != null) {
            loginUserCacheMap.remove(callback.getState());
        }
    }

    public Callback getCallbackByState(String state) {
        return callbackDao.queryByState(state);
    }

    public LoginUser getLoginUserByRequest(HttpServletRequest request) {
        LoginUser.Platform platform = getPlatformType(request);
        // 接口请求，必须存在平台信息
        Assert.notNull(platform);
        logger.info("获取 loginUser ：" + platform);
        Callback callback = new Callback();
        switch (platform) {
            case PC:
                logger.info("pc");
                String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
                if (StringUtils.isEmpty(pcState)) return null;
                callback.setState(pcState);
                break;
            case WE_MOBILE:
                logger.info("mobile");
                String weMobileState = CookieUtils.getCookie(request, WE_CHAT_STATE_COOKIE_NAME);
                if (StringUtils.isEmpty(weMobileState)) return null;
                callback.setState(weMobileState);
                break;
            case WE_MINI:
                logger.info("mini");
                String weMiniState = request.getHeader(WE_MINI_STATE_HEADER_NAME);
                if (StringUtils.isEmpty(weMiniState)) return null;
                callback.setState(weMiniState);
                break;
            default:
                callback = null;
        }
        LoginUser loginUser = getLoginUserByState(callback.getState());
        if (loginUser != null) {
            // 填充设备信息
            loginUser.setDevice(platform.getValue());
        }
        return loginUser;
    }

    /**
     * 根据请求获取当前请求所在平台
     * @param request 请求
     * @return 平台类型
     */
    public LoginUser.Platform getPlatformType(HttpServletRequest request) {
        String platformHeader = request.getHeader(PLATFORM_HEADER_NAME);
        if (platformHeader == null) {
            // 资源请求，没有 platform header，查看是否存在 cookie
            logger.info("header 中没有 platform 参数");
            String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
            if (pcState != null) {
                logger.info("pcState: {}", pcState);
                platformHeader = LoginUser.PlatformHeaderValue.PC_HEADER;
            }

            String mobileState = CookieUtils.getCookie(request, WE_CHAT_STATE_COOKIE_NAME);
            if (mobileState != null) {
                logger.info("mobileState: {}", mobileState);
                platformHeader = LoginUser.PlatformHeaderValue.WE_MOBILE_HEADER;
            }
        }
        if (platformHeader != null) {
            // header 中存在 platform 值，判断是哪个平台
            switch (platformHeader) {
                case LoginUser.PlatformHeaderValue.PC_HEADER:
                    logger.info("进入 pc");
                    return LoginUser.Platform.PC;
                case LoginUser.PlatformHeaderValue.WE_MOBILE_HEADER:
                    logger.info("进入 mobile");
                    return LoginUser.Platform.WE_MOBILE;
                case LoginUser.PlatformHeaderValue.WE_MINI_HEADER:
                    logger.info("进入 mini");
                    return LoginUser.Platform.WE_MINI;
                default:
                    logger.info("默认 pc");
                    return LoginUser.Platform.PC;
            }
        } else {
            return null;
        }
    }

    public static List<LoginUser> getAllUsers() {
        List<LoginUser> list = Lists.newArrayList();
        list.addAll(loginUserCacheMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        return list;
    }

    /**
     * 根据回调对象返回登录对象
     * @param state state
     */
    private LoginUser getLoginUserByState(String state) {
        Callback callback;
        LoginUser loginUser;

        if (loginUserCacheMap.containsKey(state)) {
            logger.info("缓存中存在用户对象");
            loginUser = loginUserCacheMap.get(state).get();
            if (loginUser == null) {
                // 软连接，存储对象被回收
                callback = callbackDao.queryByState(state);
                Profile profile = profileDao.queryByUnionId(callback.getUnionId());
                loginUser = buildLoginUserDetail(profile);
                loginUserCacheMap.put(state, new SoftReference<>(loginUser));
            }
        } else {
            logger.info("缓存中不存在用户对象，从数据库获取");
            // 缓存中不存在，直接从数据库中读取数据
            callback = callbackDao.queryByState(state);
            if (callback == null) {
                return null;
            }
            Profile profile = profileDao.queryByUnionId(callback.getUnionId());
            loginUser = buildLoginUserDetail(profile);
            loginUserCacheMap.put(state, new SoftReference<>(loginUser));
        }

        String unionId = loginUser.getUnionId();
        if (waitRefreshUnionIds.contains(unionId)) {
            waitRefreshUnionIds.remove(unionId);
            logger.info("刷新 UnionId 为 {} 用户缓存", unionId);
            Profile profile = profileDao.queryByUnionId(unionId);
            loginUser = buildLoginUserDetail(profile);
            // 将最新数据放进缓存
            loginUserCacheMap.put(state, new SoftReference<>(loginUser));
        }

        logger.info("返回用户对象: {}, {}", loginUser.getId(), loginUser.hashCode());
        return loginUser;
    }

    /**
     * 对用户信息进行过滤筛选
     */
    private LoginUser buildLoginUserDetail(Profile profile) {
        Role role = this.getUserRole(profile.getId());
        LoginUser loginUser = new LoginUser();
        loginUser.setId(profile.getId());
        loginUser.setUnionId(profile.getUnionid());
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

}
