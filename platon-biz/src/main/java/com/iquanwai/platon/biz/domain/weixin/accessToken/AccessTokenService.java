package com.iquanwai.platon.biz.domain.weixin.accesstoken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken(boolean force);
}
