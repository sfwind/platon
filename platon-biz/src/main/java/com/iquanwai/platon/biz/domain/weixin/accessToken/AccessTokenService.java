package com.iquanwai.platon.biz.domain.weixin.accessToken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken();
}
