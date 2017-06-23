package com.iquanwai.platon.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.systematism.CourseProgressService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.biz.po.systematism.ClassMember;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.util.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/5/5.
 */
@Service
public class LoginUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PC_TOKEN_COOKIE_NAME = "_qt";
    public static final String WECHAT_TOKEN_COOKIE_NAME = "_act";

    public enum Platform{
        PC(1),Wechat(2);
        private int value;
        private Platform(int value){
            this.value = value;
        }
        public int getValue(){
            return this.value;
        }
    }
    /**
     * 缓存已经登录的用户
     */
    private static Map<String, LoginUser> pcLoginUserMap = Maps.newHashMap(); // pc的登录缓存
    private static Map<String,LoginUser> wechatLoginUserMap = Maps.newHashMap();// 微信的登录缓存

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CallbackDao callbackDao;



    /**
     * 登录，就是缓存起来
     * @param sessionId sessionId
     * @param loginUser 用户
     */
    public  void login(Platform platform, String sessionId, LoginUser loginUser) {
        switch(platform){
            case PC:
                loginUser.setDevice(Constants.Device.PC);
                pcLoginUserMap.put(sessionId, loginUser);
                break;
            case Wechat:
                loginUser.setDevice(Constants.Device.MOBILE);
                wechatLoginUserMap.put(sessionId, loginUser);
                break;
        }
    }


    /**
     * 根据sessionId判断用户是否登录
     *
     * @param sessionId SessionId
     * @return 是否登录
     */
    public boolean isLogin(Platform platform, String sessionId) {
        LoginUser loginUser = this.loadUser(platform, sessionId);
        if (loginUser != null) {
//            logger.info("cookie:{},已登录,user:{},nickName:{}", sessionId, loginUser.getOpenId(), loginUser.getWeixinName());
            return true;
        } else {
            // 只有没登录时会打印一次
            logger.info("cookie:{},没有登录", sessionId);
            return false;
        }
    }

    /**
     * -1 key查不到callback，清除掉
     * -2 key查到了，但是获取不到user，应该是没点服务号
     * 1 成功
     */
    public Pair<Integer,Callback> refreshLogin(Platform platform,String sessionId){
        // 有key但是没有value，重新查一遍
        // 先检查这个cookie是否合法
        Callback callback = null;
        switch (platform) {
            case PC:
                callback = callbackDao.queryByPcAccessToken(sessionId);
                break;
            case Wechat:
                callback = callbackDao.queryByAccessToken(sessionId);
                break;
        }
        if (callback == null) {
            // 不合法
            return new MutablePair<>(-1, null);
        } else {
            // 合法，再查一遍
            Pair<Integer, LoginUser> result = getLoginUser(platform,sessionId);
            if (result.getLeft() < 0) {
                logger.info("platform:{},key:{} is lost , remove cookie", platform, sessionId);
                switch (platform) {
                    case PC:
                        pcLoginUserMap.remove(sessionId);
                        break;
                    case Wechat:
                        wechatLoginUserMap.remove(sessionId);
                        break;
                }
                return new MutablePair<>(-2, callback);
            } else {
                logger.info("platform:{},key:{} is lost , search again: {}", platform, sessionId, result.getRight());
                login(platform,sessionId, result.getRight());
                return new MutablePair<>(1, callback);
            }
        }
    }

    /**
     * 获取PCLoginUser
     *
     * @return -1:没有cookie <br/>
     * -2:accessToken无效<br/>
     * -3:没有关注<br/>
     * 1:PCLoginUser
     */
    public Pair<Integer, LoginUser> getLoginUser(HttpServletRequest request) {
        String pcToken = CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
        String wechatToken = CookieUtils.getCookie(request, LoginUserService.WECHAT_TOKEN_COOKIE_NAME);
        if (StringUtils.isEmpty(pcToken) && StringUtils.isEmpty(wechatToken)) {
            return new MutablePair<>(-1, null);
        } else {
            if (!StringUtils.isEmpty(pcToken)) {
                return getLoginUser(Platform.PC, pcToken);
            } else {
                return getLoginUser(Platform.Wechat, wechatToken);
            }
        }
    }

    public Boolean isLogin(HttpServletRequest request) {
        Platform platform = checkPlatform(request);
        String token = getToken(request);
        return isLogin(platform, token);
    }
    public String getToken(HttpServletRequest request) {
        Platform platform = checkPlatform(request);
        switch (platform) {
            case PC:return CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
            case Wechat:return CookieUtils.getCookie(request, LoginUserService.WECHAT_TOKEN_COOKIE_NAME);
        }
        return null;
    }



    public Platform checkPlatform(HttpServletRequest request) {
        String pcToken = CookieUtils.getCookie(request, LoginUserService.PC_TOKEN_COOKIE_NAME);
        String wechatToken = CookieUtils.getCookie(request, LoginUserService.WECHAT_TOKEN_COOKIE_NAME);
        if (StringUtils.isEmpty(pcToken) && StringUtils.isEmpty(wechatToken)) {
            // pcToken和wechatToken都为null则是移动
            return Platform.Wechat;
        };
        if (!StringUtils.isEmpty(pcToken)) {
            return Platform.PC;
        }
        if (!StringUtils.isEmpty(wechatToken)) {
            return Platform.Wechat;
        }
        // 两个都存在就默认为移动
        return Platform.Wechat;
    }

    public String openId(Platform platform,String accessToken){
        String openid = null;
        switch (platform) {
            case PC:
                openid = oAuthService.pcOpenId(accessToken);
                break;
            case Wechat:
                openid = oAuthService.openId(accessToken);
                break;
        }
        return openid;
    }
    /**
     * 获取PCLoginUser
     *
     * @return -1:没有cookie <br/>
     * -2:accessToken无效,没有点页面<br/>
     * -3:没有关注，一般不会走到这个<br/>
     * -4:一般是没有关注服务号
     * 1:PCLoginUser
     */
    public Pair<Integer, LoginUser> getLoginUser(Platform platform, String accessToken) {
        // 先检查有没有缓存
        LoginUser loginUser = this.loadUser(platform, accessToken);
        if (loginUser != null) {
            logger.debug("已缓存,_qt:{}", accessToken);
            return new MutablePair<>(1, loginUser);
        }

        String openid = this.openId(platform, accessToken);
        if (openid == null) {
            // 没有查到openid，一般是该用户没有关注服务号
            logger.info("accessToken:{} can't find openid", accessToken);
            return new MutablePair<>(-4, null);
        }
        Account account;
        try {
            account = accountService.getAccount(openid, false);
        } catch (NotFollowingException e) {
            return new MutablePair<>(-3, null);
        }
        logger.info("platform:{},accessToken:{},openId:{},account:{}",platform, accessToken, openid, account);
        if (account == null) {
            return new MutablePair<>(-2, null);
        }

        Profile profile = accountService.getProfile(openid, false);

        Role role = this.getUserRole(profile.getId());
        LoginUser temp = new LoginUser();
        temp.setOpenId(openid);
        temp.setHeadimgUrl(profile.getHeadimgurl());
        temp.setRealName(profile.getRealName());
        temp.setWeixinName(profile.getNickname());
        temp.setId(profile.getId());
        temp.setRole(role.getId());
        temp.setSignature(profile.getSignature());
        logger.info("user:{}", temp);
        return new MutablePair<>(1, temp);
    }

    public Role getUserRole(Integer profileId){
        Role role = accountService.getRole(profileId);
        if (role == null) {
            // 获得用户的openid，根据openid查询用户的学号
            //如果报名了训练营或者开启了RISE,返回学生角色,反之返回陌生人
            List<ClassMember> classMembers = courseProgressService.loadActiveCourse(profileId);
            List<ImprovementPlan> plans = planService.loadUserPlans(profileId);
            if (classMembers.isEmpty() && plans.isEmpty()) {
                role = Role.stranger();
            } else {
                role = Role.student();
            }
        }
        return role;
    }

    private LoginUser loadUser(Platform platform, String accessToken) {
        LoginUser loginUser = null;
        switch (platform) {
            case PC:
                loginUser = pcLoginUserMap.get(accessToken);
                break;
            case Wechat:
                loginUser = wechatLoginUserMap.get(accessToken);
                break;
        }
        return loginUser;
    }

    public LoginUser getLoginUser(String openId) {
        Profile account = accountService.getProfile(openId, false);

        if(account==null){
            logger.error("openId {} is not found in db", openId);
            return null;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(account.getId());
        loginUser.setOpenId(account.getOpenid());
        loginUser.setWeixinName(account.getNickname());
        loginUser.setHeadimgUrl(account.getHeadimgurl());
        loginUser.setRealName(account.getRealName());
        loginUser.setRole(account.getRole());
        loginUser.setSignature(account.getSignature());
        loginUser.setOpenRise(account.getOpenRise());
        loginUser.setOpenConsolidation(account.getOpenConsolidation());
        loginUser.setOpenApplication(account.getOpenApplication());
        loginUser.setRiseMember(account.getRiseMember());
        return loginUser;
    }

    public static List<LoginUser> getAllUsers(){
        List<LoginUser> list = Lists.newArrayList();
        list.addAll(pcLoginUserMap.values());
        list.addAll(wechatLoginUserMap.values());
        return list;
    }
}
