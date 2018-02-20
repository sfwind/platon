package com.iquanwai.platon.web.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.web.util.CookieUtils;
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
 * Created by 三十文
 */
@Service
public class UnionUserServiceImpl implements UnionUserService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CallbackDao callbackDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PLATFORM_HEADER_NAME = "platform";

    /** 登录用户缓存 */
    private static Map<String, SoftReference<UnionUser>> unionUserCacheMap = Maps.newHashMap();
    /** 待更新信息用户的 unionId 集合 */
    private static List<String> waitRefreshUnionIds = Lists.newArrayList();

    @Override
    public Callback getCallbackByRequest(HttpServletRequest request) {
        UnionUser.Platform platform = getPlatformType(request);
        if (platform == null){
            return null;
        }
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

    @Override
    public UnionUser getUnionUserByCallback(Callback callback) {
        UnionUser unionUser;
        String state = callback.getState();
        String unionId = callback.getUnionId();
        if (unionUserCacheMap.containsKey(state)) {
            unionUser = unionUserCacheMap.get(state).get();
            if (unionUser == null) {
                // 如果软连接对象过期，重新加载用户信息
                unionUser = buildUnionUser(callback);
                unionUserCacheMap.put(state, new SoftReference<>(unionUser));
            } else {
                if (waitRefreshUnionIds.contains(unionId)) {
                    unionUser = buildUnionUser(callback);
                    unionUserCacheMap.put(state, new SoftReference<>(unionUser));
                    waitRefreshUnionIds.remove(unionId);
                }
            }
        } else {
            unionUser = buildUnionUser(callback);
            unionUserCacheMap.put(state, new SoftReference<>(unionUser));
        }
        return unionUser;
    }

    @Override
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
                    return UnionUser.Platform.PC;
                case UnionUser.PlatformHeaderValue.MOBILE_HEADER:
                    return UnionUser.Platform.MOBILE;
                case UnionUser.PlatformHeaderValue.MINI_HEADER:
                    return UnionUser.Platform.MINI;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void updateUserByUnionId(String unionId) {
        waitRefreshUnionIds.add(unionId);
    }

    @Override
    public boolean isDocumentRequest(HttpServletRequest request) {
        return request.getHeader(PLATFORM_HEADER_NAME) == null;
    }

    @Override
    public void logout(String state) {
        unionUserCacheMap.remove(state);
    }

    @Override
    public List<UnionUser> getAllLoginUsers() {
        List<UnionUser> unionUsers = Lists.newArrayList();
        unionUsers.addAll(
                unionUserCacheMap.values().stream()
                        .filter(value -> value.get() != null)
                        .map(SoftReference::get)
                        .collect(Collectors.toList())
        );
        return unionUsers;
    }

    /**
     * 根据 unionId 获取用户对象，如果 Profile 不存在，则返回 null，将用户获取功能统一交给 confucius
     */
    private UnionUser buildUnionUser(Callback callback) {
        Profile profile = accountService.getProfileByUnionId(callback.getUnionId());

        if (profile == null) {
            boolean success = accountService.initUserByUnionId(callback.getUnionId(), false);
            if (success) {
                profile = accountService.getProfileByUnionId(callback.getUnionId());
            }
        }

        if (profile == null) {
            return null;
        }

        UnionUser unionUser = new UnionUser();
        unionUser.setId(profile.getId());
        unionUser.setOpenId(profile.getOpenid());
        unionUser.setUnionId(profile.getUnionid());
        unionUser.setNickName(profile.getNickname());
        unionUser.setHeadImgUrl(profile.getHeadimgurl());
        return unionUser;
    }

}
