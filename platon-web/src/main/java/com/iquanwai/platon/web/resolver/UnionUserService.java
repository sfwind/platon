package com.iquanwai.platon.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import com.iquanwai.platon.web.util.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

/**
 * Created by 三十文
 */
@Service
public class UnionUserService {

    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PC_STATE_COOKIE_NAME = "_qt";
    private static final String MOBILE_STATE_COOKIE_NAME = "_act";
    private static final String MINI_STATE_HEADER_NAME = "sk";

    private static final String PLATFORM_HEADER_NAME = "platform";

    /** 登录用户缓存 */
    private static Map<String, SoftReference<UnionUser>> unionUserCacheMap = Maps.newHashMap();
    /** 待更新信息用户的 unionId 集合 */
    private static List<String> waitRefreshUnionIds = Lists.newArrayList();

    /** 根据请求获取 callback 数据 */
    public Callback getCallbackByRequest(HttpServletRequest request) {
        UnionUser.Platform platform = getPlatformType(request);
        if (platform == null) return null;
        switch (platform) {
            case PC:
                String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
                return callbackDao.queryByState(pcState);
            case MOBILE:
                String mobileState = CookieUtils.getCookie(request, MOBILE_STATE_COOKIE_NAME);
                return callbackDao.queryByState(mobileState);
            case MINI:
                String miniState = request.getHeader(MINI_STATE_HEADER_NAME);
                return callbackDao.queryByState(miniState);
            default:
                return null;
        }
    }

    public UnionUser getUnionUserByCallback(Callback callback) {
        UnionUser unionUser;
        String state = callback.getState();
        String unionId = callback.getUnionId();
        if (unionUserCacheMap.containsKey(state)) {
            unionUser = unionUserCacheMap.get(state).get();
            if (unionUser == null) {
                // 如果软连接对象过期，重新加载用户信息
                unionUser = getUnionUserByUnionId(unionId);
                if (unionUser == null) return null;
                unionUserCacheMap.put(state, new SoftReference<>(unionUser));
            } else {
                if (waitRefreshUnionIds.contains(unionId)) {
                    unionUser = getUnionUserByUnionId(unionId);
                    if (unionUser == null) return null;
                    unionUserCacheMap.put(state, new SoftReference<>(unionUser));
                    waitRefreshUnionIds.remove(unionId);
                }
            }
        } else {
            unionUser = getUnionUserByUnionId(unionId);
            if (unionUser == null) return null;
            unionUserCacheMap.put(state, new SoftReference<>(unionUser));
        }
        return unionUser;
    }

    /** 获取当前所在平台 */
    public UnionUser.Platform getPlatformType(HttpServletRequest request) {
        String platformHeader = request.getHeader(PLATFORM_HEADER_NAME);
        if (platformHeader == null) {
            // 资源请求，没有 platform header，查看 cookie 值
            logger.info("资源请求，没有 platform header，查看 cookie 值");
            String pcState = CookieUtils.getCookie(request, PC_STATE_COOKIE_NAME);
            if (pcState != null) {
                logger.info("pcState: {}", pcState);
                platformHeader = UnionUser.PlatformHeaderValue.PC_HEADER;
            }

            String mobileState = CookieUtils.getCookie(request, MOBILE_STATE_COOKIE_NAME);
            if (mobileState != null) {
                logger.info("mobileState: {}", mobileState);
                platformHeader = UnionUser.PlatformHeaderValue.MOBILE_HEADER;
            }
        }

        if (platformHeader != null) {
            switch (platformHeader) {
                case UnionUser.PlatformHeaderValue.PC_HEADER:
                    logger.info("所在平台为：{}", UnionUser.PlatformHeaderValue.PC_HEADER);
                    return UnionUser.Platform.PC;
                case UnionUser.PlatformHeaderValue.MOBILE_HEADER:
                    logger.info("所在平台为：{}", UnionUser.PlatformHeaderValue.MOBILE_HEADER);
                    return UnionUser.Platform.MOBILE;
                case UnionUser.PlatformHeaderValue.MINI_HEADER:
                    logger.info("所在平台为：{}", UnionUser.PlatformHeaderValue.MINI_HEADER);
                    return UnionUser.Platform.MINI;
                default:
                    logger.info("没有匹配到对应平台");
                    return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 根据 unionId 获取用户对象，如果 Profile 不存在，则返回 null，将用户获取功能统一交给 confucius
     * @param unionId 联合 UnionId
     */
    private UnionUser getUnionUserByUnionId(String unionId) {
        Profile profile = accountService.getProfileByUnionId(unionId);
        if (profile == null) {
            return null;
        } else {
            UnionUser unionUser = new UnionUser();
            unionUser.setId(profile.getId());
            unionUser.setOpenId(profile.getOpenid());
            unionUser.setUnionId(profile.getUnionid());
            unionUser.setNickName(profile.getNickname());
            unionUser.setHeadImgUrl(profile.getHeadimgurl());
            return unionUser;
        }
    }

    private void aa(Callback callback) {
        // 链接打到 confucius
        String requestUrl = ConfigUtils.domainName() + "/wx/oauth/init/user?state=" + callback.getState();
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        String code = result.get("code").toString();
        if ("200".equals(code)) {
            unionUser = unionUserService.getUnionUserByCallback(callback);
        }
    }

}
