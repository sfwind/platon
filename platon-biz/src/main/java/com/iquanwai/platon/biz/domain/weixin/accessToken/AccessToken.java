package com.iquanwai.platon.biz.domain.weixin.accessToken;

public class AccessToken {
    public AccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public AccessToken() {
    }

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


}
