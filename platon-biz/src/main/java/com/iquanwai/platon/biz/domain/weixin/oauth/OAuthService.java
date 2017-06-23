package com.iquanwai.platon.biz.domain.weixin.oauth;

/**
 * Created by justin on 14-7-28.
 */
public interface OAuthService {
    String ACCESS_TOKEN_COOKIE_NAME = "_act";
    /**
     * 根据accessToken，获取授权用户的openid
     * */
    String openId(String accessToken);

    String pcOpenId(String act);
}
