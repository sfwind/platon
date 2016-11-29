package com.iquanwai.platon.biz.domain.weixin.oauth;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.po.Callback;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by justin on 16/8/13.
 */
@Service
public class OAuthServiceImpl implements OAuthService {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private CallbackDao callbackDao;

    private static final String REDIRECT_PATH = "/wx/oauth/code";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String redirectUrl(String callbackUrl) {
        String requestUrl = OAUTH_URL;
        Callback callback = new Callback();
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        logger.info("state is {}", state);
        callbackDao.insert(callback);

        Map<String,String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        try {
            params.put("redirect_url", URLEncoder.encode(ConfigUtils.domainName()+REDIRECT_PATH, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        params.put("state", state);
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        return requestUrl;
    }

    public String openId(String accessToken) {
        if(accessToken==null){
            return null;
        }
        Callback callback = callbackDao.queryByAccessToken(accessToken);
        if(callback==null){
            logger.error("accessToken {} is invalid", accessToken);
            return null;
        }
        return callback.getOpenid();
    }

    public String refresh(String accessToken) {
        Callback callback = callbackDao.queryByAccessToken(accessToken);
        if(callback==null){
            logger.error("accessToken {} is invalid", accessToken);
            return null;
        }
        String requestUrl = REFRESH_TOKEN_URL;

        Map<String,String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("refresh_token", callback.getRefreshToken());
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        String newAccessToken = (String)result.get("access_token");

        //刷新accessToken
        callbackDao.refreshToken(callback.getState(), newAccessToken);
        return newAccessToken;
    }

    public Callback accessToken(String code, String state) {
        Callback callback = callbackDao.queryByState(state);
        if(callback==null){
            logger.error("state {} is not found", state);
            return null;
        }
        String requestUrl = ACCESS_TOKEN_URL;

        Map<String,String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("secret", ConfigUtils.getSecret());
        params.put("code", code);
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        String accessToken = (String)result.get("access_token");
        String openid = (String)result.get("openid");
        String refreshToken = (String)result.get("refresh_token");
        //更新accessToken，refreshToken，openid
        callback.setOpenid(openid);
        callback.setRefreshToken(refreshToken);
        callback.setAccessToken(accessToken);
        callbackDao.updateUserInfo(state, accessToken, refreshToken, openid);

        // callbackUrl增加参数access_token
//        String callbackUrl = callback.getCallbackUrl();
//        callbackUrl = CommonUtils.appendAccessToken(callbackUrl, accessToken);
        return callback;
    }
}
