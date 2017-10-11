package com.iquanwai.platon.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
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
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/5/5.
 */
@Service
public class LoginUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PC_TOKEN_COOKIE_NAME = "_qt";
    public static final String WECHAT_TOKEN_COOKIE_NAME = "_act";
    public static final String ACCESS_ASK_TOKEN_COOKIE_NAME = "_ask";


    public enum Platform{
        PC(1),Wechat(2);
        private int value;
        Platform(int value){
            this.value = value;
        }
        public int getValue(){
            return this.value;
        }
    }
    /**
     * 缓存已经登录的用户
     */
    private static Map<String, SoftReference<LoginUser>> pcLoginUserMap = Maps.newHashMap(); // pc的登录缓存
    private static Map<String, SoftReference<LoginUser>> wechatLoginUserMap = Maps.newHashMap();// 微信的登录缓存
    private static List<String> waitPCRefreshOpenids = Lists.newArrayList(); //待更新openid
    private static List<String> waitWechatRefreshOpenids = Lists.newArrayList(); //待更新openid

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private RiseMemberDao riseMemberDao;


    /**
     * 登录，就是缓存起来
     * @param sessionId sessionId
     * @param loginUser 用户
     */
    public  void login(Platform platform, String sessionId, LoginUser loginUser) {
        switch(platform){
            case PC:
                loginUser.setDevice(Constants.Device.PC);
                pcLoginUserMap.put(sessionId, new SoftReference<>(loginUser));
                break;
            case Wechat:
                loginUser.setDevice(Constants.Device.MOBILE);
                wechatLoginUserMap.put(sessionId, new SoftReference<>(loginUser));
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
            // 只在未登录的时候打印
//            logger.info("cookie:{},已登录,user:{},nickName:{}", sessionId, loginUser.getOpenId(), loginUser.getWeixinName());
            return true;
        } else {
            // 只有没登录时会打印一次
            logger.info("cookie:{},没有登录,platform:{}", sessionId, platform);
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

    public void logout(String sessionId) {
        pcLoginUserMap.remove(sessionId);
    }

    public void updateWeixinUser(String openid){
        if(!waitPCRefreshOpenids.contains(openid)){
            waitPCRefreshOpenids.add(openid);
        }
        if(!waitWechatRefreshOpenids.contains(openid)){
            waitWechatRefreshOpenids.add(openid);
        }
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
        }
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
//            logger.debug("已缓存,_qt:{}", accessToken);
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

        // 重新加载loginUser
        loginUser = getLoginUser(openid, platform);
        logger.info("user:{}", loginUser);
        return new MutablePair<>(1, loginUser);
    }

    public Role getUserRole(Integer profileId){
        Role role = accountService.getRole(profileId);
        if (role == null) {
            // 获得用户的openid，根据openid查询用户的学号
            List<ImprovementPlan> plans = planService.loadUserPlans(profileId);
            if (plans.isEmpty()) {
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
                if(pcLoginUserMap.get(accessToken)!=null){
                    loginUser = pcLoginUserMap.get(accessToken).get();
                }
                // 如果数据待更新,则读取数据库
                if(loginUser!=null){
                    String openid = loginUser.getOpenId();
                    if(waitPCRefreshOpenids.contains(openid)){
                        logger.info("更新用户{}", openid);
                        loginUser = getLoginUser(openid, platform);
                        pcLoginUserMap.put(accessToken, new SoftReference<>(loginUser));
                        waitPCRefreshOpenids.remove(openid);
                    }
                }
                break;
            case Wechat:
                if(wechatLoginUserMap.get(accessToken)!=null){
                    loginUser = wechatLoginUserMap.get(accessToken).get();
                }
                // 如果数据待更新,则读取数据库
                if(loginUser!=null){
                    String openid2 = loginUser.getOpenId();
                    if(waitWechatRefreshOpenids.contains(openid2)){
                        logger.info("更新用户{}", openid2);
                        loginUser = getLoginUser(openid2, platform);
                        wechatLoginUserMap.put(accessToken, new SoftReference<>(loginUser));
                        waitWechatRefreshOpenids.remove(openid2);
                    }
                }
                break;
        }
        return loginUser;
    }

    public LoginUser getLoginUser(String openId, Platform platform) {
        Profile profile = null;
        try {
            Account temp = accountService.getAccount(openId, false);
            if (temp != null) {
                profile = accountService.getProfile(openId);
            }
        } catch (NotFollowingException e) {
            logger.error("异常，openid:{}，没有查到", openId);
        }

        if(profile==null){
            logger.error("openId {} is not found in db", openId);
            return null;
        }

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
        loginUser.setDevice(platform.getValue());
        loginUser.setRiseMember(riseMember(profile.getId()));
        return loginUser;
    }

    public static List<LoginUser> getAllUsers(){
        List<LoginUser> list = Lists.newArrayList();
        list.addAll(pcLoginUserMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        list.addAll(wechatLoginUserMap.values().stream().map(SoftReference::get).collect(Collectors.toList()));
        return list;
    }

    private Integer riseMember(Integer profileId){
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if(riseMember==null){
            return 0;
        }
        Integer memberTypeId = riseMember.getMemberTypeId();
        if(memberTypeId == null){
            return 0;
        }
        // 精英或者专业版用户
        if(memberTypeId == RiseMember.HALF || memberTypeId == RiseMember.ANNUAL
                || memberTypeId == RiseMember.ELITE || memberTypeId == RiseMember.HALF_ELITE){
            return 1;
        // 训练营用户
        } else if(memberTypeId == RiseMember.CAMP){
            return 3;
        // 小课用户
        } else if(memberTypeId == RiseMember.COURSE){
            return 2;
        }

        return 0;
    }
}
