package com.iquanwai.platon.biz.domain.weixin.oauth;

/**
 * Created by justin on 14-7-28.
 */
public interface OAuthService {
    String MOBILE_STATE_COOKIE_NAME = "_act";
    String PC_STATE_COOKIE_NAME = "_qt";

    /**
     * 根据 state，获取授权用户的openid
     */
    String openId(String mobileState);

    String pcOpenId(String pcState);
}
